package com.nxoim.decomposite.core.common.navigation.animations.stack

import androidx.compose.runtime.Immutable
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages the animation data cache.
 */
@Immutable
class AnimationDataHandler<Key : Any>(
	val animationDataRegistry: AnimationDataRegistry<Key>
) {
	/**
	 * Cache for child anim prerequisites so they arent recalculated
	 * hundreds of times per second during gestures
	 */
	val childAnimPrerequisites = hashMapOf<Key, ChildAnimPrerequisites>()

	fun removeAnimationDataFromCache(target: Key) {
		animationDataRegistry.remove(target)
		childAnimPrerequisites.remove(target)
	}

	fun removeStaleAnimationDataCache(nonStale: List<Key>) {
		val stale = childAnimPrerequisites.filter { it.key !in nonStale }.map { it.key }
		stale.forEach(::removeAnimationDataFromCache)
	}

	fun updateChildAnimPrerequisites(key: Key, allowAnimation: Boolean, inStack: Boolean) {
		childAnimPrerequisites[key] = ChildAnimPrerequisites(allowAnimation, inStack)
	}

	suspend inline fun updateGestureDataInScopes(backGestureData: BackGestureEvent) {
		withContext(currentCoroutineContext()) {
			animationDataRegistry.forEach { (configuration, animationData) ->
				val prerequisites = childAnimPrerequisites[configuration]
					?: ChildAnimPrerequisites(allowAnimation = false, inStack = false)

				if (prerequisites.inStack && prerequisites.allowAnimation) {
					animationData.scopes().forEach { (_, scope) ->
						launch { scope.onBackGesture(backGestureData) }
					}
				}
			}
		}
	}
}