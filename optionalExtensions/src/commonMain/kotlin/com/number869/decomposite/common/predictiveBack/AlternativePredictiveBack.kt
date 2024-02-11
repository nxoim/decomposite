package com.number869.decomposite.common.predictiveBack

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimatable
import com.arkivanov.essenty.backhandler.BackEvent
import com.number869.decomposite.common.softSpring
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Alternative to [predictiveBackAnimatable]. This implementation provides unprocessed
 * gesture offset, progress, their approximate velocities, an animated gesture progress like the one in
 * [materialPredictiveBackAnimatable], and an additional animation progress value that animates between 0f and 1f
 * after the gesture is completed for post-gesture animations. Animated gesture progress
 * and post-gesture animation progress use gesture's velocity to animate, providing
 * a more natural feel to the animation.
 */
@Stable
@ExperimentalDecomposeApi
fun alternativePredictiveBackAnimatable(
    initialBackEvent: BackEvent,
    animationSpec: FiniteAnimationSpec<Float> = softSpring(),
    exitModifier: BackEvent.(AnimationData) -> Modifier,
    enterModifier: BackEvent.(AnimationData) -> Modifier,
): PredictiveBackAnimatable = AlternativePredictiveBackAnimatable(
    initialBackEvent = initialBackEvent,
    animationSpec = animationSpec,
    getExitModifier = exitModifier,
    getEnterModifier = enterModifier,
)

@Stable
@ExperimentalDecomposeApi
internal class AlternativePredictiveBackAnimatable(
    initialBackEvent: BackEvent,
    private val animationSpec: FiniteAnimationSpec<Float>,
    private val getExitModifier: BackEvent.(AnimationData) -> Modifier,
    private val getEnterModifier: BackEvent.(AnimationData) -> Modifier,
) : PredictiveBackAnimatable {
    private val postGestureReleaseAnimationProgress = Animatable(0f)
    private val animatedGestureProgress = Animatable(0f)
    private val rawGestureProgress = Animatable(0f)
    private val offsetX = Animatable(0f)
    private val offsetY = Animatable(0f)
    private var backEvent by mutableStateOf(initialBackEvent)

    private val animationData get() = AnimationData(
        postGestureAnimationProgress = postGestureReleaseAnimationProgress.value,
        animatedGestureProgress = animatedGestureProgress.value,
        gestureOffsetVelocity = Offset(offsetX.velocity, offsetY.velocity)
    )
    override val exitModifier: Modifier get() = backEvent.getExitModifier(animationData)
    override val enterModifier: Modifier get() = backEvent.getEnterModifier(animationData)

    override suspend fun animate(event: BackEvent) {
        backEvent = event
        animatedGestureProgress.snapTo(event.progress)

        // to track velocity
        coroutineScope {
            launch { rawGestureProgress.animateTo(event.progress) }
            launch { offsetX.animateTo(event.touchX) }
            launch { offsetY.animateTo(event.touchY) }
        }
    }

    override suspend fun cancel() {
        coroutineScope {
            launch { animatedGestureProgress.animateTo(0f, animationSpec) }
            launch { postGestureReleaseAnimationProgress.animateTo(0f, animationSpec) }
        }
    }

    override suspend fun finish() {
        coroutineScope {
            launch {
                animatedGestureProgress.animateTo(
                    1f,
                    animationSpec,
                    rawGestureProgress.velocity
                )
            }

            launch {
                postGestureReleaseAnimationProgress.animateTo(
                    1f,
                    animationSpec,
                    rawGestureProgress.velocity
                )
            }
        }
    }
}

data class AnimationData(
    val postGestureAnimationProgress: Float,
    val animatedGestureProgress: Float,
    val gestureOffsetVelocity: Offset
)