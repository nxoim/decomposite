package com.number869.decomposite.common

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.*
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler
import com.number869.decomposite.common.predictiveBack.alternativePredictiveBackAnimatable
import com.number869.decomposite.core.common.navigation.NavController

internal fun <T> softSpring() = spring<T>(1.8f, 2500f)

fun slide(
    animationSpec: FiniteAnimationSpec<Float> = softSpring(),
    orientation: Orientation = Orientation.Horizontal,
    targetOffset: Int = 64
)= stackAnimator(animationSpec = animationSpec) { factor, _, content ->
    content(
        Modifier.graphicsLayer {
            if (orientation == Orientation.Vertical) {
                translationY = targetOffset * factor
            } else {
                translationX = targetOffset * factor
            }
        }
    )
}

fun cleanFadeAndSlide(
    animationSpec: FiniteAnimationSpec<Float> = softSpring(),
    orientation: Orientation = Orientation.Horizontal,
    targetOffsetDp: Int = 64
)= stackAnimator(animationSpec = animationSpec) { progress, direction, content ->
    content(
        Modifier.graphicsLayer {
            if (orientation == Orientation.Vertical) {
                translationY = (targetOffsetDp * density) * progress
            } else {
                translationX = (targetOffsetDp * density) * progress
            }

            alpha = if (direction.isFront) (1f - progress * 2) else (1f + progress * 2)
        }
    )
}

@OptIn(ExperimentalDecomposeApi::class)
fun <C : Any, T : Any> scaleFadePredictiveBackAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
    cleanFade: Boolean = false,
    minimumScale: Float = 0.85f,
    maxHorizontalOffsetDp: Int = 42,
    fullAlphaTransitionFraction: Float = 1f,
    fallbackAnimation: StackAnimation<C, T> = stackAnimation { _ ->
        fade(softSpring()) + scale(softSpring(), minimumScale, minimumScale)
    }
): StackAnimation<C, T> {
    val customCurve = CubicBezierEasing(1f, 0.2f, 0.0f, 1f)

    return predictiveBackAnimation(
        backHandler = backHandler,
        fallbackAnimation = fallbackAnimation,
        selector = { initialBackEvent, _, _ ->
            alternativePredictiveBackAnimatable(
                initialBackEvent = initialBackEvent,
                exitModifier = {
                    Modifier.graphicsLayer {
                        val progress = if (cleanFade)
                            it.animatedGestureProgress * 2f
                        else
                            it.animatedGestureProgress

                        alpha = 1f - customCurve.transform(progress * fullAlphaTransitionFraction)

                        val scale = minimumScale + (1f - minimumScale) * (1f - progress)
                        // what
                        val prbndbdubd = lerp(-fullAlphaTransitionFraction, 1f, progress) + fullAlphaTransitionFraction * (1f - progress)

                        val offsetX = if (swipeEdge == BackEvent.SwipeEdge.LEFT) {
                            (maxHorizontalOffsetDp * density) * prbndbdubd
                        } else {
                            -(maxHorizontalOffsetDp * density) * prbndbdubd
                        }

                        scaleX = scale
                        scaleY = scale

                        translationX = offsetX
                    }
                },
                enterModifier = {
                    Modifier.graphicsLayer {
                        val progress = if (cleanFade)
                            lerp(-(1f / fullAlphaTransitionFraction), 1f, it.animatedGestureProgress)
                        else
                            it.animatedGestureProgress

                        alpha = customCurve.transform(progress * fullAlphaTransitionFraction)

                        val scale = minimumScale + (1f - minimumScale) * progress
                        val prbndbdubd = lerp(-fullAlphaTransitionFraction, 1f, progress) + fullAlphaTransitionFraction * (1f - progress)
                        val offsetX = if (swipeEdge == BackEvent.SwipeEdge.LEFT) {
                            -(maxHorizontalOffsetDp * density) * (1f - prbndbdubd)
                        } else {
                            (maxHorizontalOffsetDp * density) * (1f - prbndbdubd)
                        }

                        scaleX = scale
                        scaleY = scale

                        translationX = offsetX
                    }
                },
            )
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalDecomposeApi::class)
fun <C : Any, T : Any> NavController<C>.scaleFadePredictiveBackAnimation(
    cleanFade: Boolean = false,
    minimumScale: Float = 0.85f,
    maxHorizontalOffsetDp: Int = 42,
    fullAlphaTransitionFraction: Float = 3f,
    fallbackAnimation: StackAnimation<C, T> = stackAnimation { _ ->
        fade(softSpring()) + scale(softSpring(), minimumScale, minimumScale)
    }
) = scaleFadePredictiveBackAnimation(
    backHandler = this::backHandler.get(),
    onBack = { navigateBack() },
    cleanFade = cleanFade,
    minimumScale = minimumScale,
    maxHorizontalOffsetDp = maxHorizontalOffsetDp,
    fullAlphaTransitionFraction = fullAlphaTransitionFraction,
    fallbackAnimation = fallbackAnimation
)