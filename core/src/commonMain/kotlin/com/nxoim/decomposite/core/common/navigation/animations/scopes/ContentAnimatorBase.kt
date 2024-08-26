package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType
import com.nxoim.decomposite.core.common.navigation.animations.Direction
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.none
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

/**
 * A base class for creating custom content animators. This class handles common state management
 * and animation logic, reducing boilerplate code and ensuring consistent animation behavior.
 *
 * It manages the animation state, handles back gestures, and provides a mechanism for
 * launching and controlling animations.
 *
 * Example:
 * ```kotlin
 * class SimpleContentAnimator(
 *     initialIndex: Int,
 *     initialIndexFromTop: Int
 * ) : ContentAnimatorBase(initialIndex, initialIndexFromTop) {
 *     // animated value that uses the index from top as a target.
 *     // -1 = not in stack
 *     // 0 = at the very top of the stack, visible content
 *     // 1 = in the back of the stack
 * 	   private val gestureAnimationProgressAnimatable = Animatable(initialIndexFromTop.toFloat())
 *
 *     // take the back gesture progress and animate the value
 *     override val onGestureActions = OnGestureActions(
 *         onProgress = { newBackEvent ->
 *             gestureAnimationProgressAnimatable
 * 				.snapTo(indexFromTop - newBackEvent.progress)
 *         }
 *     )
 *
 *     // animate to a value when stack changes or gesture is cancelled
 *     override val onAnimateToTargetRequest = OnAnimateToTargetRequest {
 *         gestureAnimationProgressAnimatable.animateTo(indexFromTop.toFloat())
 *     }
 *
 * 	   // provide a value for the StackAnimator to make an AnimatedVisibilityScope
 *     override val animationProgressForScope by gestureAnimationProgressAnimatable.asState()
 *
 *     // expose values to the animator creator, to be used in the animated modifier
 *     inner class Scope() {
 *         val animationProgress by gestureAnimationProgressAnimatable.asState()
 *     }
 * }
 * ```
 * @param initialIndex The initial index of the item in the stack.
 * @param initialIndexFromTop The initial index of the item from the top of the stack, with 0 being the top.
 *
 * @property onGestureActions Actions to be performed when a back gesture occurs.
 * @property onAnimateToTargetRequest Request to animate to a specific target.
 */
abstract class ContentAnimatorBase(
	initialIndex: Int,
	initialIndexFromTop: Int
) : ContentAnimator {
	final override var indexFromTop by mutableIntStateOf(initialIndexFromTop)
		private set

	final override var index by mutableIntStateOf(initialIndex)
		private set

	private var previousIndexFromTop by mutableStateOf(
		when {
			index == 0 -> 0
			initialIndexFromTop == 0 -> -1
			else -> null
		}
	)

	final override var animationStatus by mutableStateOf(
		AnimationStatus(
			previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
			location = toItemLocation(initialIndexFromTop.coerceAtLeast(0)),
			direction = if (index == 0) Direction.None else Direction.Inwards,
			animationType = if (index == 0) AnimationType.None else AnimationType.Passive
		)
	)
		private set

	protected abstract val onGestureActions: OnGestureActions
	protected abstract val onAnimateToTargetRequest: OnAnimateToTargetRequest

	/**
	 * Handles back gestures and updates the animation state accordingly.
	 *
	 * @param backGesture The back gesture event.
	 */
	final override suspend fun onBackGesture(backGesture: BackGestureEvent) {
		when (backGesture) {
			is BackGestureEvent.OnBackStarted -> {
				updateAnimationStatus(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)
				onGestureActions.onStarted(backGesture.event)
			}

			is BackGestureEvent.OnBackProgressed -> {
				// an animation can be kinda cancelled (see AnimationType.PassiveCancelling)
				// meaning the direction and type might be updated to none
				// while a gesture is in progress. this makes sure that doesn't happen
				updateAnimationStatus(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)
				onGestureActions.onProgressed(backGesture.event)
			}

			BackGestureEvent.None,
			BackGestureEvent.OnBackCancelled -> {
				updateAnimationStatus(
					direction = Direction.Inwards,
					animationType = AnimationType.PassiveCancelling
				)

				onGestureActions.onCancelled()

				launchAnimations(onAnimateToTargetRequest)
			}

			BackGestureEvent.OnBack -> {
				onGestureActions.onCompleted()

				updateAnimationStatus(
					direction = Direction.Outwards,
					animationType = AnimationType.Passive
				)
			}
		}
	}

	/**
	 * Updates the animation data and triggers the animation to the new target.
	 *
	 * @param newIndex The new index of the item in the stack.
	 * @param newIndexFromTop The new index of the item from the top of the stack.
	 */
	final override suspend fun update(newIndex: Int, newIndexFromTop: Int) {
		val newDirection = when {
			newIndexFromTop <= -1 || newIndexFromTop < indexFromTop -> Direction.Outwards
			newIndexFromTop > indexFromTop -> Direction.Inwards
			else -> Direction.None
		}

		previousIndexFromTop = indexFromTop

		index = newIndex
		indexFromTop = newIndexFromTop

		updateAnimationStatus(
			previousIndexFromTop = previousIndexFromTop,
			indexFromTop = indexFromTop,
			direction = newDirection,
			animationType = if (newDirection.none) AnimationType.None else AnimationType.Passive
		)

		launchAnimations(onAnimateToTargetRequest)
	}

	private fun updateAnimationStatus(
		previousIndexFromTop: Int? = this.previousIndexFromTop,
		indexFromTop: Int = this.indexFromTop,
		direction: Direction,
		animationType: AnimationType
	) {
		animationStatus = AnimationStatus(
			previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
			location = toItemLocation(indexFromTop),
			direction = direction,
			animationType = animationType
		)
	}

	private suspend fun launchAnimations(
		animationToTarget: OnAnimateToTargetRequest
	) = coroutineScope {
		animationToTarget.animation(this)

		updateAnimationStatus(
			direction = Direction.None,
			animationType = AnimationType.None
		)

		animationToTarget.onAnimationEndAndStatusUpdate?.invoke(this)
	}

	private fun toItemLocation(indexFromTop: Int): ItemLocation = when {
		indexFromTop < 0 -> ItemLocation.Outside(indexFromTop)
		indexFromTop == 0 -> ItemLocation.Top
		indexFromTop > 0 -> ItemLocation.Back(indexFromTop)
		else -> error("Unexpected indexFromTop value: $indexFromTop")
	}

	/**
	 * Represents a request to animate to a specific target.
	 *
	 * @param onAnimationEndAndStatusUpdate An optional function to be
	 * called after the animation ends and the status is updated.
	 * @param animation The animation function to be executed.
	 */
	protected class OnAnimateToTargetRequest(
		val onAnimationEndAndStatusUpdate: (suspend CoroutineScope.() -> Unit)? = null,
		val animation: suspend CoroutineScope.() -> Unit
	)

	/**
	 * Represents the actions to be performed when a back gesture occurs.
	 *
	 * @param onStarted Action to be performed when the back gesture starts.
	 * @param onProgressed Action to be performed as the back gesture progresses.
	 * @param onCancelled Action to be performed when the back gesture is cancelled.
	 * @param onCompleted Action to be performed when the back gesture is completed.
	 */
	protected class OnGestureActions(
		val onStarted: suspend (BackEvent) -> Unit = { },
		val onProgressed: suspend (BackEvent) -> Unit = { },
		val onCancelled: suspend () -> Unit = { },
		val onCompleted: suspend () -> Unit = { }
	)
}