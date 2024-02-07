package com.number869.decomposite.common

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.*
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler

fun slide(
    animationSpec: FiniteAnimationSpec<Float> = tween(),
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
    animationSpec: FiniteAnimationSpec<Float> = tween(),
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
    minimumScale: Float = 0.9f,
    maxHorizontalOffsetDp: Int = 32,
    fallbackAnimation: StackAnimation<C, T> = stackAnimation { _ ->
        fade(tween(200)) + scale(tween(200), minimumScale, minimumScale)
    }
): StackAnimation<C, T> {
    val customCurve = CubicBezierEasing(1f, 0.2f, 0.1f, 1f)

    return predictiveBackAnimation(
        backHandler = backHandler,
        fallbackAnimation = fallbackAnimation,
        selector = { initialBackEvent, _, _ ->
            predictiveBackAnimatable(
                initialBackEvent = initialBackEvent,
                exitModifier = { gestureProgress, swipeEdge ->
                    Modifier.graphicsLayer {
                        val progress = if (cleanFade)
                            gestureProgress * 2f
                        else
                            gestureProgress

                        alpha = 1f - if (cleanFade) progress else customCurve.transform(progress)

                        val scale = minimumScale + (1f - minimumScale) * (1f - progress)
                        val offsetX = if (swipeEdge == BackEvent.SwipeEdge.LEFT) {
                            (maxHorizontalOffsetDp * density) * progress
                        } else {
                            -(maxHorizontalOffsetDp * density) * progress
                        }

                        scaleX = scale
                        scaleY = scale

                        translationX = offsetX
                    }
                },
                enterModifier = { gestureProgress, swipeEdge ->
                    Modifier.graphicsLayer {
                        val progress = if (cleanFade)
                            lerp(-1f, 1f, gestureProgress)
                        else
                            gestureProgress

                        alpha = if (cleanFade) progress else customCurve.transform(progress)

                        val scale = minimumScale + (1f - minimumScale) * progress
                        val offsetX = if (swipeEdge == BackEvent.SwipeEdge.LEFT) {
                            -(maxHorizontalOffsetDp * density) * (1f - progress)
                        } else {
                            (maxHorizontalOffsetDp * density) * (1f - progress)
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