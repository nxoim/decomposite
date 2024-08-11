package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.outside
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.top
import com.nxoim.decomposite.core.common.navigation.animations.softSpring
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * [animationSpec] is still used for the gesture cancellation animation
 */
@Suppress("UNCHECKED_CAST")
@OptIn(InternalDecomposeApi::class)
internal fun materialContainerMorphContentAnimator(
	animationSpec: AnimationSpec<Float> = softSpring(),
	renderUntil: Int = 1,
	requireVisibilityInBackstack: Boolean = false,
	block: MaterialContainerMorphContentAnimator.Scope.() -> Modifier
) = ContentAnimations(
	listOf(
		ContentAnimator(
			// 1 instance per animation spec per destination
			key = "MaterialContainerMorphContentAnimatorScope",
			renderUntil = renderUntil,
			requireVisibilityInBackstack = requireVisibilityInBackstack,
			animatorScopeFactory = { initialIndex, initialIndexFromTop ->
				MaterialContainerMorphContentAnimator(
					initialIndex,
					initialIndexFromTop,
					animationSpec,
				)
			},
			animationModifier = { block(this.Scope()) }
		)
	)
)

internal class MaterialContainerMorphContentAnimator(
	private val initialIndex: Int,
	private val initialIndexFromTop: Int,
	private val animationSpec: AnimationSpec<Float>
) : BasicContentAnimator(initialIndex, initialIndexFromTop) {
	private val velocityTracker = VelocityTracker()

	// standard progress
	private val animationProgressAnimatable = Animatable(indexFromTop.toFloat())

	// progress - gesture progress
	private val gestureAnimationProgressAnimatable = Animatable(indexFromTop.toFloat())
	private val swipeOffsetAnimatable = Animatable(IntOffset.Zero, IntOffset.VectorConverter)

	private var initialSwipeOffset by mutableStateOf(Offset.Zero)

	private var swipeEdge by mutableStateOf(BackEvent.SwipeEdge.LEFT)

	override val animationProgressForScope by gestureAnimationProgressAnimatable.asState()

	override val onBackGestures = OnGestureActions(
		onStarted = {
			// stop all animations
			animationProgressAnimatable.stop()
			gestureAnimationProgressAnimatable.stop()
			swipeOffsetAnimatable.stop()

			initialSwipeOffset = Offset(it.touchX, it.touchY)
			swipeEdge = it.swipeEdge
			velocityTracker.resetTracking()
		},
		onProgressed = { newBackEvent ->
			if (animationStatus.location.top) {
				gestureAnimationProgressAnimatable
					.snapTo(animationProgressAnimatable.value -newBackEvent.progress)

				swipeOffsetAnimatable.snapTo(
					IntOffset(
						(newBackEvent.touchX - initialSwipeOffset.x).roundToInt(),
						(newBackEvent.touchY - initialSwipeOffset.y).roundToInt()
					)
				)

				withFrameMillis { frameTimeMillis ->
					velocityTracker.addPosition(
						timeMillis = frameTimeMillis,
						position = Offset(animationProgressAnimatable.value, 0f)
					)
				}
			}
		}
	)

	override val onRequestAnimationToTarget = OnAnimateToTargetRequest {
		// don't need this delay if the item is to be removed, which is when location is outside
		if (!animationStatus.location.outside) {
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

			joinAll(animationProgressAnimation, gestureProgressAnimation)
		}
	}

	// exposing some values to the animator creator
	inner class Scope {
		val animationProgress by animationProgressAnimatable.asState()
		val gestureAnimationProgress by gestureAnimationProgressAnimatable.asState()
		val swipeOffset by swipeOffsetAnimatable.asState()
		val swipeEdge get() = this@MaterialContainerMorphContentAnimator.swipeEdge
	}
}