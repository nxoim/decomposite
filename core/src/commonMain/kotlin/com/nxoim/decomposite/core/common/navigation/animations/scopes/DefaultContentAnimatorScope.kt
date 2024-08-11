package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.softSpring
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
@Suppress("UNCHECKED_CAST")
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
			animationModifier = block as ContentAnimatorScope.() -> Modifier
		)
	)
)

@Immutable
class DefaultContentAnimatorScope(
	private val initialIndex: Int,
	private val initialIndexFromTop: Int,
	private val animationSpec: AnimationSpec<Float>
) : BasicContentAnimatorScope(initialIndex, initialIndexFromTop) {
	private val initial get() = index == 0

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

	override val onBackGestures = OnGestureActions(
		onStarted = {
			// stop all animations
			animationProgressAnimatable.stop()
			gestureAnimationProgressAnimatable.stop()

			initialSwipeOffset = Offset(it.touchX, it.touchY)
			backEvent = it
			velocityTracker.resetTracking()
		},
		onProgressed = { newBackEvent ->
			backEvent = newBackEvent
			gestureAnimationProgressAnimatable
				.snapTo(animationProgress - newBackEvent.progress)

			withFrameMillis { frameTimeMillis ->
				velocityTracker.addPosition(
					timeMillis = frameTimeMillis,
					position = Offset(gestureAnimationProgress, 0f)
				)
			}
		},
	)

	override val onRequestAnimationToTarget = OnAnimationToTargetRequest(
		onStatusUpdate = { backEvent = BackEvent() }
	) {
		val initialAnimationVelocity = velocityTracker.calculateVelocity().x

		val gestureProgressAnimation = launch {
			gestureAnimationProgressAnimatable.animateTo(
				targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
				animationSpec = animationSpec,
				initialVelocity = initialAnimationVelocity
			)
		}

		val animationProgressAnimation = launch {
			animationProgressAnimatable.animateTo(
				targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
				animationSpec = animationSpec,
				initialVelocity = initialAnimationVelocity
			)
		}

		joinAll(animationProgressAnimation, gestureProgressAnimation)
	}
}