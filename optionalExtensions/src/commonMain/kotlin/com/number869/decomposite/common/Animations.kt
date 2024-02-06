package com.number869.decomposite.common

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
    targetOffset: Int = 64
)= stackAnimator(animationSpec = animationSpec) { progress, direction, content ->
    content(
        Modifier.graphicsLayer {
            if (orientation == Orientation.Vertical) {
                translationY = targetOffset * progress
            } else {
                translationX = targetOffset * progress
            }

            alpha = if (direction.isFront) (1f - progress * 2) else (1f + progress * 2)
        }
    )
}

@OptIn(ExperimentalDecomposeApi::class)
fun <C : Any, T : Any> scaleFadePredictiveBackAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
    minimumScale: Float = 0.8f,
    maxHorizontalOffsetDp: Int = 16,
    fallbackAnimation: StackAnimation<C, T> = stackAnimation { _ ->
        fade(tween(200)) + scale(tween(200), minimumScale, minimumScale)
    }
): StackAnimation<C, T> {
    val customCurve = CubicBezierEasing(1f, 0.1f, 0.2f, 1.05f)

    return predictiveBackAnimation(
        backHandler = backHandler,
        fallbackAnimation = fallbackAnimation,
        selector = { initialBackEvent, _, _ ->
            predictiveBackAnimatable(
                initialBackEvent = initialBackEvent,
                exitModifier = { progress, swipeEdge ->
                    Modifier.graphicsLayer {
                        alpha = 1f - customCurve.transform(progress)

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
                enterModifier = { progress, swipeEdge ->
                    Modifier.graphicsLayer {
                        alpha = customCurve.transform(progress)

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