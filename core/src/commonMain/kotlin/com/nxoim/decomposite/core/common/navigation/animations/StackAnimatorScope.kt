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
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import com.nxoim.decomposite.core.common.navigation.LocalNavigationRoot
import com.nxoim.decomposite.core.common.navigation.animations.scopes.ContentAnimatorScope
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import com.nxoim.decomposite.core.common.ultils.rememberRetained
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


// it's important to use the local instance keeper rather than of the children's for the
// scopes not to be recreated which is useful in case the exit animation of a config is
// interrupted by the same config appearing in the stack again while the animation is running
/**
 * Creates a retained instance of a [StackAnimatorScope]. The [StackAnimator] relies on
 * the provided [key] for the removal of stale animation data.
 */
@Composable
fun <Key : Any, Instance : Any> rememberStackAnimatorScope(
	key: String,
	stack: () -> List<Instance>,
	onBackstackChange: (stackEmpty: Boolean) -> Unit,
	itemKey: (Instance) -> Key,
	excludedDestinations: (Instance) -> Boolean,
	animations: DestinationAnimationsConfiguratorScope<Instance>.() -> ContentAnimations,
	allowBatchRemoval: Boolean = true,
): StackAnimatorScope<Key, Instance> {
	val animationDataRegistry = rememberRetained("$key StackAnimatorScope") {
		AnimationDataRegistry<Key>()
	}

	return remember("$key StackAnimatorScope") {
		StackAnimatorScope(
			key,
			stack,
			onBackstackChange,
			itemKey,
			excludedDestinations,
			animations,
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
class StackAnimatorScope<Key : Any, Instance : Any>(
	val key: String?,
	private val stack: () -> List<Instance>,
	private val onBackstackChange: (stackEmpty: Boolean) -> Unit,
	val itemKey: (Instance) -> Key,
	private val excludedDestinations: (Instance) -> Boolean,
	private val animations: DestinationAnimationsConfiguratorScope<Instance>.() -> ContentAnimations,
	private val allowBatchRemoval: Boolean,
	val animationDataRegistry: AnimationDataRegistry<Key>
) {
	var sourceStack by mutableStateOf(stack())
		private set

	private val removingChildren = mutableStateListOf<Key>()
	val visibleCachedChildren = mutableStateMapOf<Key, Instance>().apply {
		putAll(stack().fastFilterNot(excludedDestinations).associateBy(itemKey))
	}

	val childAnimPrerequisites = hashMapOf<Key, ChildAnimPrerequisites>()

	private fun removeAnimationDataFromCache(target: Key) {
		animationDataRegistry.remove(target)
		childAnimPrerequisites.remove(target)
	}

	/**
	 * Removes stale animation data. Stale animation data is the data that was left over
	 * during a configuration change that is no longer used (no longer exists in the source stack)
	 */
	private fun removeStaleAnimationDataCache(nonStale: List<Key>) {
		val stale = childAnimPrerequisites.filter { it.key !in nonStale }.map { it.key }
		stale.fastForEach(::removeAnimationDataFromCache)
	}

	suspend inline fun updateGestureDataInScopes(backGestureData: BackGestureEvent) =
		withContext(currentCoroutineContext()) {
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


	private fun updateChildAnimPrerequisites(key: Key, allowAnimation: Boolean, inStack: Boolean) {
		childAnimPrerequisites[key] = ChildAnimPrerequisites(allowAnimation, inStack)
	}

	suspend fun observeAndUpdateAnimatorData() {
		// check on startup if there's animation data left for nonexistent children, which
		// can happen during a configuration change
		removeStaleAnimationDataCache(nonStale = sourceStack.fastMap(itemKey))

		snapshotFlow(stack).collect { newStackRaw ->
			onBackstackChange(newStackRaw.size <= 1)

			val oldStack = sourceStack
			val newStack = newStackRaw.fastFilterNot(excludedDestinations)

			val childrenToRemove = oldStack
				.fastFilter { it !in newStack && itemKey(it) !in removingChildren }
			val batchRemoval = childrenToRemove.size > 1 && allowBatchRemoval

			// cancel removal of items that appeared again in the stack
			removingChildren.removeAll(newStackRaw.fastMap(itemKey))

			if (batchRemoval) {
				// remove from cache and everything all children, except the last one,
				// which will be animated
				val itemsToRemoveImmediately =
					childrenToRemove.subList(0, childrenToRemove.size - 1)
				itemsToRemoveImmediately.fastForEach {
					visibleCachedChildren.remove(itemKey(it))
				}
				removingChildren.add(itemKey(childrenToRemove.last()))
			} else {
				childrenToRemove.fastForEach { removingChildren.add(itemKey(it)) }
			}

			sourceStack = newStackRaw

			visibleCachedChildren.putAll(newStack.associateBy(itemKey))
		}
	}

	@Composable
	fun createStackItemState(key: Key): State<ItemState> {
		val inSourceStack = !removingChildren.contains(key)

		val index = if (inSourceStack)
			sourceStack.indexOfFirst { itemKey(it) == key }
		else
			-(removingChildren.indexOf(key) + 1)

		val indexFromTop = if (inSourceStack)
			sourceStack.size - index - 1
		else
			-(removingChildren.indexOf(key) + 1)

		val allAnimations = animations(
			DestinationAnimationsConfiguratorScope(
				stack().elementAtOrNull(index - 1),
				visibleCachedChildren[key]!!,
				stack().elementAtOrNull(index + 1),
				removingChildren.fastMap { visibleCachedChildren[key]!! },
				LocalNavigationRoot.current.screenInformation
			)
		)
		val animData = animationDataRegistry.getOrCreateAnimationData(
			key = key,
			source = allAnimations,
			initialIndex = index,
			initialIndexFromTop = if (indexFromTop == 0 && index != 0)
				-1
			else
				indexFromTop
		)


		val allowingAnimation = indexFromTop <= (allAnimations.items.minOf { it.renderUntil })

		val animating by remember {
			derivedStateOf { animData.scopes.values.any { it.animationStatus.animating } }
		}

		val displaying = remember(animating, allowingAnimation) {
			val requireVisibilityInBack =
				animData.requireVisibilityInBackstacks.fastAny { it }
			val renderingBack = allowingAnimation && animating
			val renderTopAndAnimatedBack = indexFromTop < 1 || renderingBack
			if (requireVisibilityInBack) allowingAnimation else renderTopAndAnimatedBack
		}


		remember(animating) {
			if (!inSourceStack && !animating) {
				visibleCachedChildren.remove(key)
				removingChildren.remove(key)
				removeAnimationDataFromCache(key)
			}
		}


		LaunchedEffect(allowingAnimation, inSourceStack) {
			updateChildAnimPrerequisites(key, allowingAnimation, inSourceStack)
		}

		// launch animations if there's changes
		LaunchedEffect(indexFromTop, index) {
			animData.scopes.forEach { (_, scope) ->
				launch {
					scope.update(
						newIndex = index,
						newIndexFromTop = indexFromTop,
					)
				}
			}
		}

		return rememberUpdatedState(
			ItemState(
				index,
				indexFromTop,
				displaying,
				allowingAnimation,
				inSourceStack,
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
		val animationData: AnimationData
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

class AnimationDataRegistry<Key : Any> {
	private val animationData = hashMapOf<Key, AnimationData>()
	private val scopeRegistry = hashMapOf<Pair<Key, String>, ContentAnimatorScope>()

	fun getOrCreateAnimationData(
		key: Key,
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

			source.items.fastForEach { animator ->
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

	fun get(key: Key) = animationData[key]
		?: error("No animation data for $key in AnimationDataRegistry")

	fun forEach(item: (Map.Entry<Key, AnimationData>) -> Unit) = animationData.forEach(item)

	fun remove(key: Key) {
		animationData.remove(key)
		scopeRegistry.keys.removeAll { it.first == key }
	}
}

@Suppress("BanInlineOptIn") // Treat Kotlin Contracts as non-experimental.
@OptIn(ExperimentalContracts::class)
private inline fun <T> List<T>.fastFilterNot(predicate: (T) -> Boolean): List<T> {
	contract { callsInPlace(predicate) }
	val target = ArrayList<T>(size)
	fastForEach {
		if (!predicate(it)) target += (it)
	}
	return target
}