package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.Direction
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.none
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation
import com.nxoim.decomposite.core.common.navigation.animations.softSpring
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * Creates an animation scope.
 *
 * [renderUntil] Controls content rendering based on it's position in the stack and animation state.
 * Content at or above [renderUntil] is always rendered if it's the item is index 0 or -1 (top or outside).
 *
 * If [requireVisibilityInBackstack] is false (which is by default) - the top and outside items
 * are rendered at all times while the backstack items are only rendered if they're being animated.
 *
 * If [requireVisibilityInBackstack] is set to false - will be visible even when it's not animated
 * (note that if you're combining animations, like fade() + scale(), if one of them has [requireVisibilityInBackstack]
 * set to false - ALL items will be visible while in backstack as if all animations have [requireVisibilityInBackstack]
 * set to true).
 */
@OptIn(InternalDecomposeApi::class)
fun contentAnimator(
	animationSpec: AnimationSpec<Float> = softSpring(),
	renderUntil: Int = 1,
	requireVisibilityInBackstack: Boolean = false,
	block: DefaultContentAnimatorScope.() -> Modifier
) = ContentAnimations(
	listOf(
		ContentAnimator(
			key = animationSpec.hashString() + "DefaultContentAnimator", // 1 instance per animation spec
			renderUntil = renderUntil,
			requireVisibilityInBackstack = requireVisibilityInBackstack,
			animatorScopeFactory = { initialIndex, initialIndexFromTop ->
				DefaultContentAnimatorScope(initialIndex, initialIndexFromTop, animationSpec)
			},
			animationModifier = block
		)
	)
)

@Immutable
class DefaultContentAnimatorScope(
	private val initialIndex: Int,
	private val initialIndexFromTop: Int,
	private val animationSpec: AnimationSpec<Float>
) : ContentAnimatorScope {
	override var indexFromTop by mutableIntStateOf(initialIndexFromTop)
		private set

	override var index by mutableIntStateOf(initialIndex)
		private set
	private val initial get() = index == 0

	private var previousIndexFromTop by mutableStateOf(
		when {
			initial -> 0
			initialIndexFromTop == 0 -> -1
			else -> null
		}
	)

	private val velocityTracker = VelocityTracker()

	// standard progress
	private val animationProgressAnimatable = Animatable(if (initial) 0f else -1f)

	// progress minus gesture progress
	private val gestureAnimationProgressAnimatable = Animatable(animationProgressAnimatable.value)

	var backEvent by mutableStateOf(BackEvent())
		private set

	private var initialSwipeOffset by mutableStateOf(Offset.Zero)

	val animationProgress by animationProgressAnimatable.asState()
	val gestureAnimationProgress by gestureAnimationProgressAnimatable.asState()
	override val animationProgressForScope
		get() = gestureAnimationProgress

	val swipeOffset
		get() = Offset(
			initialSwipeOffset.x - backEvent.touchX,
			initialSwipeOffset.y - backEvent.touchY
		)

	override var animationStatus by mutableStateOf(
		AnimationStatus(
			previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
			location = toItemLocation(initialIndexFromTop.coerceAtLeast(0)),
			direction = if (initial) Direction.None else Direction.Inwards,
			animationType = if (initial) AnimationType.None else AnimationType.Passive
		)
	)
		private set

	override suspend fun onBackGesture(
		backGesture: BackGestureEvent
	) {
		when (backGesture) {
			is BackGestureEvent.OnBackStarted -> {
				// stop all animations
				animationProgressAnimatable.stop()
				gestureAnimationProgressAnimatable.stop()

				initialSwipeOffset = Offset(backGesture.event.touchX, backGesture.event.touchY)
				backEvent = backGesture.event
				velocityTracker.resetTracking()

				updateAnimationStatusAfterAllChanges(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)
			}

			is BackGestureEvent.OnBackProgressed -> {
				backEvent = backGesture.event

				// an animation can be kinda cancelled (see AnimationType.PassiveCancelling)
				// meaning the direction and type might be updated to none
				// while a gesture is in progress. this makes sure that doesn't happen
				updateAnimationStatusAfterAllChanges(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)

				gestureAnimationProgressAnimatable
					.snapTo(animationProgress - backGesture.event.progress)


				withFrameMillis { frameTimeMillis ->
					velocityTracker.addPosition(
						timeMillis = frameTimeMillis,
						position = Offset(gestureAnimationProgress, 0f)
					)
				}
			}

			BackGestureEvent.None,
			BackGestureEvent.OnBackCancelled -> {
				updateAnimationStatusAfterAllChanges(
					direction = Direction.Inwards,
					animationType = AnimationType.PassiveCancelling
				)

				animateToTarget()
			}

			BackGestureEvent.OnBack -> {
				updateAnimationStatusAfterAllChanges(
					direction = Direction.Outwards,
					animationType = AnimationType.Passive
				)
			}
		}
	}

	override suspend fun update(
		newIndex: Int,
		newIndexFromTop: Int,
	) {
		val newDirection = when {
			newIndexFromTop <= -1 || newIndexFromTop < indexFromTop -> Direction.Outwards
			newIndexFromTop > indexFromTop -> Direction.Inwards
			else -> Direction.None
		}

		previousIndexFromTop = indexFromTop

		index = newIndex
		indexFromTop = newIndexFromTop

		updateAnimationStatusAfterAllChanges(
			previousIndexFromTop = previousIndexFromTop,
			indexFromTop = indexFromTop,
			direction = newDirection,
			animationType = if (newDirection.none) AnimationType.None else AnimationType.Passive
		)

		animateToTarget()
	}

	private suspend fun animateToTarget() = coroutineScope {
		val velocity = velocityTracker.calculateVelocity().x

		val gestureProgressAnimation = launch {
			gestureAnimationProgressAnimatable.animateTo(
				targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
				animationSpec = animationSpec,
				initialVelocity = velocity
			)
		}

		val animationProgressAnimation = launch {
			animationProgressAnimatable.animateTo(
				targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
				animationSpec = animationSpec,
				initialVelocity = velocity
			)
		}

		launch {
			// for a moment this block will be called upon OnBack because that's animateTo's
			// intended behavior, meaning these will be called unintentionally, unintentionally
			// updating animation status. adding a delay compensates for this
			withFrameNanos { }
			joinAll(animationProgressAnimation, gestureProgressAnimation)

			updateAnimationStatusAfterAllChanges(
				direction = Direction.None,
				animationType = AnimationType.None
			)

			backEvent = BackEvent()
		}
	}

	private fun updateAnimationStatusAfterAllChanges(
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
}

private fun toItemLocation(indexFromTop: Int): ItemLocation = when {
	indexFromTop < 0 -> ItemLocation.Outside(indexFromTop)
	indexFromTop == 0 -> ItemLocation.Top
	indexFromTop > 0 -> ItemLocation.Back(indexFromTop)
	else -> error("Unexpected indexFromTop value: $indexFromTop")
}