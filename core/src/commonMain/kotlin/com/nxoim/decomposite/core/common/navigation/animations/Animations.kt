package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.scopes.contentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.scopes.materialContainerMorphContentAnimator

fun softSpring() = spring(1.8f, 2500f, 0.0005f)

fun emptyAnimation(
    renderUntil: Int = Int.MAX_VALUE,
    requireVisibilityInBackstack: Boolean = false,
) = contentAnimator(tween(0), renderUntil, requireVisibilityInBackstack) { Modifier }

fun fade(
    animationSpec: AnimationSpec<Float> = softSpring(),
    clean: Boolean = true,
    animateUsingGestures: Boolean = true
) = contentAnimator(animationSpec) {
    Modifier.graphicsLayer {
        val grade = if (clean) 2 else 1
        val progress = when {
            animateUsingGestures -> gestureAnimationProgress
            else -> animationProgress
        }

        alpha = 1f + (progress * grade).let { -it * it }
    }
}

fun scale(
    animationSpec: AnimationSpec<Float> = softSpring(),
    minimumScale: Float = 0.9f,
    renderUntil: Int = Int.MAX_VALUE,
    affectByGestures: Boolean = true,
    requireVisibilityInBackstack: Boolean = false
) = contentAnimator(animationSpec, renderUntil, requireVisibilityInBackstack) {
    Modifier.graphicsLayer {
        val progress = if (affectByGestures) gestureAnimationProgress else animationProgress
        val scale = (progress - (minimumScale * progress)).let {
            1f + (-it * it)
        }

        scaleX = scale
        scaleY = scale
    }
}

fun slide(
    animationSpec: AnimationSpec<Float> = softSpring(),
    orientation: Orientation = Orientation.Horizontal,
    targetOffsetDp: Int = 64,
    dependOnSwipeEdge: Boolean = false,
    renderUntil: Int = Int.MAX_VALUE,
    requireVisibilityInBackstack: Boolean = false
) = contentAnimator(animationSpec, renderUntil, requireVisibilityInBackstack) {
    Modifier.graphicsLayer {
        val progress = if (dependOnSwipeEdge && backEvent.swipeEdge == BackEvent.SwipeEdge.RIGHT)
            gestureAnimationProgress
        else
            -gestureAnimationProgress

        if (orientation == Orientation.Vertical) {
            translationY = (targetOffsetDp * density) * progress
        } else {
            translationX = (targetOffsetDp * density) * progress
        }
    }
}

fun iosLikeSlide(
    animationSpec: AnimationSpec<Float> = softSpring(),
    backStackSlideFraction: Float = 0.25f
) = contentAnimator(animationSpec, renderUntil = 3) {
    Modifier
        .drawWithContent {
            val color = Color.Black.copy((gestureAnimationProgress * 0.2f).coerceIn(0f, 1f))
            drawContent()
            drawRect(color)
        }
        .graphicsLayer {
            val itemOffset = -size.width * gestureAnimationProgress
            val backstackItemOffset = (size.width * (1f - backStackSlideFraction)) * gestureAnimationProgress.coerceIn(0f, 1f)

            translationX = backstackItemOffset + itemOffset
        }
}

fun cleanSlideAndFade(
    animationSpec: AnimationSpec<Float> = softSpring(),
    orientation: Orientation = Orientation.Horizontal,
    targetOffsetDp: Int = 64,
    animateFadeUsingGestures: Boolean = false,
) = fade(
    clean = true,
    animateUsingGestures = animateFadeUsingGestures,
    animationSpec = animationSpec
) + slide(
    orientation = orientation,
    targetOffsetDp = targetOffsetDp,
    animationSpec = animationSpec
)

fun DestinationAnimationsConfiguratorScope<*>.materialContainerMorph(
    sharedElementModifier: Modifier = Modifier,
    fallbackCornerRadiusDp: Int = 16,
) = materialContainerMorphContentAnimator {
    Modifier.then(sharedElementModifier)
        .drawWithContent {
            val color = Color.Black.copy((animationProgress * 0.2f).coerceIn(0f, 1f))
            drawContent()
            drawRect(color)
        }
        .graphicsLayer {
            val padding = (8 * density)
            val gestureProgress = (-gestureAnimationProgress).coerceIn(0f, 1f)
            val reversedProgress = (1f - animationProgress)

            val backLayerScale = 1f - (0.025f * animationProgress).coerceIn(0f, 1f)
            val frontScale = (gestureAnimationProgress.coerceAtMost(0f) * 0.1f)

            val offsetX = ((((size.width / 20) - padding) * gestureProgress) - reversedProgress).let {
                if (swipeEdge == BackEvent.SwipeEdge.LEFT) it else -it
            }
            val offsetY = (((swipeOffset.y / 20) - padding) * gestureProgress) * reversedProgress

            scaleX = backLayerScale + frontScale
            scaleY = backLayerScale + frontScale

            translationX = offsetX
            translationY = offsetY

            val devicesShape = screenInformation.screenShape.path?.let {
                GenericShape { _, _ -> addPath(it); close() }
            }
            val devicesCorners by lazy {
                screenInformation.screenShape.corners?.run {
                    RoundedCornerShape(
                        topLeftPx.toFloat(),
                        topRightPx.toFloat(),
                        bottomLeftPx.toFloat(),
                        bottomRightPx.toFloat()
                    )
                }
            }
            val animatedFallbackRadius by lazy {
                (fallbackCornerRadiusDp * density) *
                        (-gestureAnimationProgress).coerceIn(0f, 1f)
            }

            val screenSize = Size(
                screenInformation.widthPx.toFloat(),
                screenInformation.heightPx.toFloat()
            )

            clip = true
            shape = if (this.size == screenSize)
                devicesShape ?: devicesCorners ?: RoundedCornerShape(animatedFallbackRadius)
            else
                RoundedCornerShape(animatedFallbackRadius)
        }
}



