package com.nxoim.decomposite.core.common.navigation.animations.stack

import androidx.compose.runtime.Immutable
import com.arkivanov.essenty.backhandler.BackEvent
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

	inner class GestureUpdateHandler {
		suspend inline fun dispatchOnStart(
			newBackEvent: BackEvent
		) = withContext(currentCoroutineContext()) {
			animationDataRegistry.forEach { (configuration, animationData) ->
				val prerequisites = childAnimPrerequisites[configuration]
					?: ChildAnimPrerequisites(allowAnimation = false, inStack = false)

				if (prerequisites.inStack && prerequisites.allowAnimation) {
					animationData.scopes().forEach { (_, scope) ->
						launch { scope.onBackGestureStarted(newBackEvent) }
					}
				}
			}
		}

		suspend inline fun dispatchOnProgressed(
			newBackEvent: BackEvent
		) = withContext(currentCoroutineContext()) {
			animationDataRegistry.forEach { (configuration, animationData) ->
				val prerequisites = childAnimPrerequisites[configuration]
					?: ChildAnimPrerequisites(allowAnimation = false, inStack = false)

				if (prerequisites.inStack && prerequisites.allowAnimation) {
					animationData.scopes().forEach { (_, scope) ->
						launch { scope.onBackGestureProgressed(newBackEvent) }
					}
				}
			}
		}

		suspend inline fun dispatchOnCancelled() = withContext(currentCoroutineContext()) {
			animationDataRegistry.forEach { (configuration, animationData) ->
				val prerequisites = childAnimPrerequisites[configuration]
					?: ChildAnimPrerequisites(allowAnimation = false, inStack = false)

				if (prerequisites.inStack && prerequisites.allowAnimation) {
					animationData.scopes().forEach { (_, scope) ->
						launch { scope.onBackGestureCancelled() }
					}
				}
			}
		}

		suspend inline fun dispatchOnCompleted() = withContext(currentCoroutineContext()) {
			animationDataRegistry.forEach { (configuration, animationData) ->
				val prerequisites = childAnimPrerequisites[configuration]
					?: ChildAnimPrerequisites(allowAnimation = false, inStack = false)

				if (prerequisites.inStack && prerequisites.allowAnimation) {
					animationData.scopes().forEach { (_, scope) ->
						launch { scope.onBackGestureConfirmed() }
					}
				}
			}
		}
	}
}