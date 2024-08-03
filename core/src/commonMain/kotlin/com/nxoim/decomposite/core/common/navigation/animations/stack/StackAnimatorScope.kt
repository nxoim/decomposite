package com.nxoim.decomposite.core.common.navigation.animations.stack

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastMap
import com.nxoim.decomposite.core.common.navigation.LocalNavigationRoot
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.DestinationAnimationsConfiguratorScope
import com.nxoim.decomposite.core.common.navigation.animations.scopes.ContentAnimatorScope
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.launch


/**
 * Creates an instance of a [StackAnimatorScope]. It manages instance caching and animations.
 */
@Composable
fun <Key : Any, Instance : Any> rememberStackAnimatorScope(
	stack: () -> List<Instance>,
	onBackstackChange: (stackEmpty: Boolean) -> Unit,
	itemKey: (Instance) -> Key,
	excludedDestinations: (Instance) -> Boolean,
	animations: DestinationAnimationsConfiguratorScope<Instance>.() -> ContentAnimations,
	allowBatchRemoval: Boolean = true,
	animationDataRegistry: AnimationDataRegistry<Key> = remember { AnimationDataRegistry() }
) = remember() {
	StackAnimatorScope(
		stack,
		onBackstackChange,
		itemKey,
		excludedDestinations,
		animations,
		allowBatchRemoval,
		animationDataRegistry
	)
}

/**
 * Manages the children's animation state and modifiers. Creates instances of animator scopes
 * avoiding duplicates.
 */
@Immutable
class StackAnimatorScope<Key : Any, Instance : Any>(
	private val stack: () -> List<Instance>,
	private val onBackstackChange: (stackEmpty: Boolean) -> Unit,
	val itemKey: (Instance) -> Key,
	private val excludedDestinations: (Instance) -> Boolean,
	private val animations: DestinationAnimationsConfiguratorScope<Instance>.() -> ContentAnimations,
	private val allowBatchRemoval: Boolean,
	val animationDataRegistry: AnimationDataRegistry<Key>
) {
	private val stackCacheManager = StackCacheManager(
		initialStack = stack(),
		itemKey = itemKey,
		excludedDestinations = excludedDestinations,
		allowBatchRemoval = allowBatchRemoval
	)
	val animationDataHandler = AnimationDataHandler(animationDataRegistry)

	val sourceStack get() = stackCacheManager.sourceStack
	val visibleCachedChildren = stackCacheManager.visibleCachedChildren as Map<Key, Instance>

	suspend inline fun updateGestureDataInScopes(backGestureData: BackGestureEvent) =
		animationDataHandler.updateGestureDataInScopes(backGestureData)

	suspend fun observeAndUpdateAnimatorData() {
		// check on startup if there's animation data left for nonexistent children, which
		// can happen during a configuration change
		animationDataHandler.removeStaleAnimationDataCache(sourceStack.map(itemKey))

		snapshotFlow(stack).collect { newStackRaw ->
			onBackstackChange(newStackRaw.size <= 1)
			stackCacheManager.updateVisibleCachedChildren(newStackRaw)
		}
	}

	@Composable
	fun createStackItemState(key: Key): State<ItemState> {
		val inSourceStack = !stackCacheManager.removingChildren.contains(key)

		val index = if (inSourceStack)
			sourceStack.indexOfFirst { itemKey(it) == key }
		else
			-(stackCacheManager.removingChildren.indexOf(key) + 1)

		val indexFromTop = if (inSourceStack)
			sourceStack.size - index - 1
		else
			-(stackCacheManager.removingChildren.indexOf(key) + 1)

		val allAnimations = animations(
			DestinationAnimationsConfiguratorScope(
				previousChild = stack().elementAtOrNull(index - 1),
				currentChild = visibleCachedChildren[key]!!,
				nextChild = stack().elementAtOrNull(index + 1),
				// this is more expensive than just storing instances,
				// but makes sure the instance data is always up to date
				// in the lambda
				exitingChildren = remember(stackCacheManager.removingChildren) {
					{
						stackCacheManager
							.removingChildren
							.fastMap { visibleCachedChildren[key]!! }
					}
				},
				screenInformation = LocalNavigationRoot.current.screenInformation
			)
		)

		val animData = remember(allAnimations) {
			animationDataRegistry.getOrCreateAnimationData(
				key = key,
				source = allAnimations,
				initialIndex = index,
				initialIndexFromTop = if (indexFromTop == 0 && index != 0)
					-1
				else
					indexFromTop
			)
		}

		val allowingAnimation = indexFromTop <= (allAnimations.items.minOf { it.renderUntil })

		val animating by remember {
			derivedStateOf { animData.scopes().values.any { it.animationStatus.animating } }
		}

		val displaying = remember(animating, allowingAnimation) {
			val requireVisibilityInBack =
				animData.requireVisibilityInBackstacks().fastAny { it }
			val renderingBack = allowingAnimation && animating
			val renderTopAndAnimatedBack = indexFromTop < 1 || renderingBack
			if (requireVisibilityInBack) allowingAnimation else renderTopAndAnimatedBack
		}

		remember(animating) {
			if (!inSourceStack && !animating) {
				stackCacheManager.removeAllRelatedToItem(key)
				animationDataHandler.removeAnimationDataFromCache(key)
			}
		}

		LaunchedEffect(allowingAnimation, inSourceStack) {
			animationDataHandler.updateChildAnimPrerequisites(key, allowingAnimation, inSourceStack)
		}

		// launch animations if there's changes
		LaunchedEffect(indexFromTop, index) {
			animData.scopes().forEach { (_, scope) ->
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
	val scopes: () -> Map<String, ContentAnimatorScope>,
	val modifiers: () -> List<Modifier>,
	val renderUntils: () -> List<Int>,
	val requireVisibilityInBackstacks: () -> List<Boolean>,
)

data class ChildAnimPrerequisites(
	val allowAnimation: Boolean,
	val inStack: Boolean
)