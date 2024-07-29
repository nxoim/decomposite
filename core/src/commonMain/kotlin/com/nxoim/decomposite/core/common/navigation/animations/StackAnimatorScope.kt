package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastMap
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.nxoim.decomposite.core.common.navigation.LocalNavigationRoot
import com.nxoim.decomposite.core.common.navigation.animations.scopes.ContentAnimatorScope
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import com.nxoim.decomposite.core.common.ultils.rememberRetained
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// it's important to use the local instance keeper rather than of the children's for the
// scopes not to be recreated which is useful in case the exit animation of a config is
// interrupted by the same config appearing in the stack again while the animation is running
/**
 * Creates a retained instance of a [StackAnimatorScope]. The [StackAnimator] relies on
 * the provided [key] for the removal of stale animation data.
 */
@Composable
fun <C : Any, T : Any> rememberStackAnimatorScope(
	key: String,
	stackState: State<ChildStack<C, T>>,
	onBackstackChange: (stackEmpty: Boolean) -> Unit,
	excludeStartingDestination: Boolean = false,
	allowBatchRemoval: Boolean = true,
): StackAnimatorScope<C, T> {
	val animationDataRegistry = rememberRetained("$key StackAnimatorScope") {
		AnimationDataRegistry<C>()
	}

	return remember("$key StackAnimatorScope") {
		StackAnimatorScope(
			key,
			stackState,
			onBackstackChange,
			excludeStartingDestination,
			allowBatchRemoval,
			animationDataRegistry
		)
	}
}

/**
 * Manages the children's animation state and modifiers. Creates instances of animator scopes
 * avoiding duplicates.
 */
@Immutable
class StackAnimatorScope<C : Any, T : Any>(
	val key: String?,
	private val stackState: State<ChildStack<C, T>>,
	private val onBackstackChange: (stackEmpty: Boolean) -> Unit,
	private val excludeStartingDestination: Boolean,
	private val allowBatchRemoval: Boolean,
	val animationDataRegistry: AnimationDataRegistry<C>
) {
	var sourceStack by mutableStateOf(stackState.value)
		private set

	val removingChildren = mutableStateListOf<C>()
	val visibleCachedChildren = mutableStateMapOf<C, Child.Created<C, T>>().apply {
		putAll(
			stackState.value.items.subList(
				if (excludeStartingDestination) 1 else 0,
				stackState.value.items.size
			).associateBy { it.configuration }
		)
	}

	val childAnimPrerequisites = hashMapOf<C, ChildAnimPrerequisites>()

	fun getOrCreateAnimationData(
		key: C,
		source: ContentAnimations,
		initialIndex: Int,
		initialIndexFromTop: Int
	) = animationDataRegistry.getOrCreateAnimationData(
		key,
		source,
		initialIndex,
		initialIndexFromTop
	)

	internal fun removeAnimationDataFromCache(target: C) {
		animationDataRegistry.remove(target)
		childAnimPrerequisites.remove(target)
	}

	/**
	 * Removes stale animation data. Stale animation data is the data that was left over
	 * during a configuration change that is no longer used (no longer exists in the source stack)
	 */
	private fun removeStaleAnimationDataCache(nonStale: List<C>) {
		val stale = childAnimPrerequisites.filter { it.key !in nonStale }.map { it.key }
		stale.forEach(::removeAnimationDataFromCache)
	}

	suspend inline fun updateGestureDataInScopes(backGestureData: BackGestureEvent) =
		withContext(Dispatchers.Default) {
			kotlin.runCatching {
				animationDataRegistry.forEach { (configuration, animationData) ->
					val prerequisites =
						childAnimPrerequisites[configuration] ?: ChildAnimPrerequisites(
							allowAnimation = false,
							inStack = false
						)

					if (prerequisites.inStack && prerequisites.allowAnimation) {
						animationData.scopes.forEach { (_, scope) ->
							launch { scope.onBackGesture(backGestureData) }
						}
					}
				}
			}
		}


	fun updateChildAnimPrerequisites(configuration: C, allowAnimation: Boolean, inStack: Boolean) {
		childAnimPrerequisites[configuration] = ChildAnimPrerequisites(allowAnimation, inStack)
	}

	suspend fun observeAndUpdateAnimatorData() = withContext(Dispatchers.Default) {
		// check on startup if there's animation data left for nonexistent children, which
		// can happen during a configuration change
		removeStaleAnimationDataCache(nonStale = sourceStack.items.fastMap { it.configuration })

		snapshotFlow { stackState.value }.collect { newStackRaw ->
			onBackstackChange(newStackRaw.items.size <= 1)

			val oldStack = sourceStack.items
			val newStack = newStackRaw.items.subList(
				if (excludeStartingDestination) 1 else 0,
				stackState.value.items.size
			)

			val childrenToRemove =
				oldStack.filter { it !in newStack && it.configuration !in removingChildren }
			val batchRemoval = childrenToRemove.size > 1 && allowBatchRemoval

			// cancel removal of items that appeared again in the stack
			removingChildren.removeAll(newStackRaw.items.map { it.configuration })

			if (batchRemoval) {
				// remove from cache and everything all children, except the last one,
				// which will be animated
				val itemsToRemoveImmediately =
					childrenToRemove.subList(0, childrenToRemove.size - 1)
				itemsToRemoveImmediately.forEach { (configuration, _) ->
					visibleCachedChildren.remove(configuration)
				}
				removingChildren.add(childrenToRemove.last().configuration)
			} else {
				childrenToRemove.forEach {
					removingChildren.add(it.configuration)
				}
			}

			sourceStack = newStackRaw

			visibleCachedChildren.putAll(newStack.associateBy { it.configuration })
		}
	}

	@Composable
	fun itemState(
		child: C,
		animations: DestinationAnimationsConfiguratorScope<C>.() -> ContentAnimations,
	): State<ItemState> {
		val inStack = !removingChildren.contains(child)

		val index = if (inStack)
			sourceStack.items.indexOfFirst { it.configuration == child }
		else
			-(removingChildren.indexOf(child) + 1)

		val indexFromTop = if (inStack)
			sourceStack.items.size - index - 1
		else
			-(removingChildren.indexOf(child) + 1)

		val allAnimations = animations(
			DestinationAnimationsConfiguratorScope(
				sourceStack.items.elementAt(index - 1).configuration,
				child,
				sourceStack.items.elementAt(index + 1).configuration,
				removingChildren,
				LocalNavigationRoot.current.screenInformation
			)
		)
		val animData = remember(allAnimations) {
			getOrCreateAnimationData(
				key = child,
				source = allAnimations,
				initialIndex = index,
				initialIndexFromTop = if (indexFromTop == 0 && index != 0)
					-1
				else
					indexFromTop
			)
		}

		val allowingAnimation = indexFromTop <= (animData.renderUntils.min())

		val animating by remember {
			derivedStateOf {
				animData.scopes.any { it.value.animationStatus.animating }
			}
		}

		val displaying = remember(animating, allowingAnimation) {
			val requireVisibilityInBack =
				animData.requireVisibilityInBackstacks.fastAny { it }
			val renderingBack = allowingAnimation && animating
			val renderTopAndAnimatedBack = indexFromTop < 1 || renderingBack
			if (requireVisibilityInBack) allowingAnimation else renderTopAndAnimatedBack
		}

		LaunchedEffect(allowingAnimation, inStack) {
			updateChildAnimPrerequisites(
				child,
				allowingAnimation,
				inStack
			)
		}

		return rememberUpdatedState(
			ItemState(
				index,
				indexFromTop,
				displaying,
				allowingAnimation,
				inStack,
				animData
			)
		)
	}

	data class ItemState(
		val index: Int,
		val indexFromTop: Int,
		val displaying: Boolean,
		val allowingAnimation: Boolean,
		val inStack: Boolean,
		val animationData:  AnimationData
	)
}

@Immutable
data class AnimationData(
	val scopes: Map<String, ContentAnimatorScope>,
	val modifiers: List<Modifier>,
	val renderUntils: List<Int>,
	val requireVisibilityInBackstacks: List<Boolean>,
)

data class ChildAnimPrerequisites(
	val allowAnimation: Boolean,
	val inStack: Boolean
)

class AnimationDataRegistry<C : Any> {
	private val animationData = hashMapOf<C, AnimationData>()
	private val scopeRegistry = hashMapOf<Pair<C, String>, ContentAnimatorScope>()

	fun getOrCreateAnimationData(
		key: C,
		source: ContentAnimations,
		initialIndex: Int,
		initialIndexFromTop: Int
	): AnimationData {
		// check if we already have existing data for this key
		val existingData = animationData[key]
		if (existingData != null) {
			// if we have existing data, update it
			val updatedScopes = mutableMapOf<String, ContentAnimatorScope>()
			val updatedModifiers = mutableListOf<Modifier>()
			val updatedRenderUntils = mutableListOf<Int>()
			val updatedRequireVisibilityInBackstacks = mutableListOf<Boolean>()

			source.items.forEach { animator ->
				// get existing or create the scope for this animator
				val scopeKey = Pair(key, animator.key)
				val scope = scopeRegistry.getOrPut(scopeKey) {
					animator.animatorScopeFactory(initialIndex, initialIndexFromTop)
				}
				updatedScopes[animator.key] = scope
				updatedModifiers.add(
					(animator.animationModifier as ContentAnimatorScope.() -> Modifier).invoke(
						scope
					)
				)
				updatedRenderUntils.add(animator.renderUntil)
				updatedRequireVisibilityInBackstacks.add(animator.requireVisibilityInBackstack)
			}

			val newData = AnimationData(
				updatedScopes,
				updatedModifiers,
				updatedRenderUntils,
				updatedRequireVisibilityInBackstacks
			)
			animationData[key] = newData

			return newData
		} else {
			// if we don't have existing data, create a new one
			val scopes = mutableMapOf<String, ContentAnimatorScope>()
			val modifiers = mutableListOf<Modifier>()
			val renderUntils = mutableListOf<Int>()
			val requireVisibilityInBackstacks = mutableListOf<Boolean>()

			source.items.forEach { animator ->
				val scopeKey = Pair(key, animator.key)
				val scope = scopeRegistry.getOrPut(scopeKey) {
					animator.animatorScopeFactory(initialIndex, initialIndexFromTop)
				}
				scopes[animator.key] = scope
				@Suppress("UNCHECKED_CAST") // because it's never Nothing.()
				modifiers.add(
					(animator.animationModifier as ContentAnimatorScope.() -> Modifier).invoke(
						scope
					)
				)
				renderUntils.add(animator.renderUntil)
				requireVisibilityInBackstacks.add(animator.requireVisibilityInBackstack)
			}

			val newData =
				AnimationData(scopes, modifiers, renderUntils, requireVisibilityInBackstacks)
			animationData[key] = newData
			return newData
		}
	}

	fun get(key: C) = animationData[key]
		?: error("No animation data for $key in AnimationDataRegistry")

	fun forEach(item: (Map.Entry<C, AnimationData>) -> Unit) = animationData.forEach { item(it) }

	fun remove(key: C) {
		animationData.remove(key)
		scopeRegistry.keys.removeAll { it.first == key }
	}
}