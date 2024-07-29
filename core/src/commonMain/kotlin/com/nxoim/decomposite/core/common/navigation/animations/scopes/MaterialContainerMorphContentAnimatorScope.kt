package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.Direction
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.none
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.outside
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.top
import com.nxoim.decomposite.core.common.navigation.animations.softSpring
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * [animationSpec] is still used for the gesture cancellation animation
 */
@OptIn(InternalDecomposeApi::class)
internal fun materialContainerMorphContentAnimator(
	animationSpec: AnimationSpec<Float> = softSpring(),
	renderUntil: Int = 1,
	requireVisibilityInBackstack: Boolean = false,
	block: MaterialContainerMorphContentAnimatorScope.() -> Modifier
) = ContentAnimations(
	listOf(
		ContentAnimator(
			// 1 instance per animation spec per destination
			key = "MaterialContainerMorphContentAnimatorScope",
			renderUntil = renderUntil,
			requireVisibilityInBackstack = requireVisibilityInBackstack,
			animatorScopeFactory = { initialIndex, initialIndexFromTop ->
				MaterialContainerMorphContentAnimatorScope(
					initialIndex,
					initialIndexFromTop,
					animationSpec,
				)
			},
			animationModifier = block
		)
	)
)

internal class MaterialContainerMorphContentAnimatorScope(
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
	private val animationProgressAnimatable = Animatable(indexFromTop.toFloat())

	// progress - gesture progress
	private val gestureAnimationProgressAnimatable = Animatable(indexFromTop.toFloat())
	private val swipeOffsetAnimatable = Animatable(IntOffset.Zero, IntOffset.VectorConverter)

	private var initialSwipeOffset by mutableStateOf(Offset.Zero)

	val animationProgress by animationProgressAnimatable.asState()
	val gestureAnimationProgress by gestureAnimationProgressAnimatable.asState()
	val swipeOffset by swipeOffsetAnimatable.asState()
	var swipeEdge by mutableStateOf(BackEvent.SwipeEdge.LEFT)
		private set

	override val animationProgressForScope get() = gestureAnimationProgress

	override var animationStatus by mutableStateOf(
		AnimationStatus(
			previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
			location = toItemLocation(initialIndexFromTop.coerceAtLeast(0)),
			direction = if (initial) Direction.None else Direction.Inwards,
			animationType = if (initial) AnimationType.None else AnimationType.Passive
		)
	)
		private set

	override suspend fun onBackGesture(backGesture: BackGestureEvent) {
		when (backGesture) {
			is BackGestureEvent.OnBackStarted -> {
				// stop all animations
				animationProgressAnimatable.stop()
				gestureAnimationProgressAnimatable.stop()
				swipeOffsetAnimatable.stop()

				initialSwipeOffset = Offset(backGesture.event.touchX, backGesture.event.touchY)
				swipeEdge = backGesture.event.swipeEdge
				velocityTracker.resetTracking()

				updateAnimationStatusAfterAllChanges(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)
			}

			is BackGestureEvent.OnBackProgressed -> {
				// an animation can be kinda cancelled (see AnimationType.PassiveCancelling)
				// meaning the direction and type might be updated to none
				// while a gesture is in progress. this makes sure that doesn't happen
				updateAnimationStatusAfterAllChanges(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)

				if (animationStatus.location.top) {
					gestureAnimationProgressAnimatable.snapTo(animationProgress - backGesture.event.progress)

					swipeOffsetAnimatable.snapTo(
						IntOffset(
							(backGesture.event.touchX - initialSwipeOffset.x).roundToInt(),
							(backGesture.event.touchY - initialSwipeOffset.y).roundToInt()
						)
					)

					withFrameMillis { frameTimeMillis ->
						velocityTracker.addPosition(
							timeMillis = frameTimeMillis,
							position = Offset(gestureAnimationProgress, 0f)
						)
					}
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
		val gestureProgressAnimation = launch {
			gestureAnimationProgressAnimatable.animateTo(
				targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
				animationSpec = animationSpec,
				initialVelocity = 0.1f + velocityTracker.calculateVelocity().x
			)
		}

		val animationProgressAnimation = launch {
			animationProgressAnimatable.animateTo(
				targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
				animationSpec = animationSpec,
				initialVelocity = 0.1f + velocityTracker.calculateVelocity().x
			)
		}

		launch {
			// for a moment this block will be called upon OnBack because that's animateTo's
			// intended behavior, meaning these will be called unintentionally, unintentionally
			// updating animation status. adding a delay compensates for this.

			// don't need this delay if the item is to be removed, which is when location is outside
			if (!animationStatus.location.outside) {
				withFrameNanos { }
				joinAll(animationProgressAnimation, gestureProgressAnimation)
			}

			updateAnimationStatusAfterAllChanges(
				direction = Direction.None,
				animationType = AnimationType.None
			)
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