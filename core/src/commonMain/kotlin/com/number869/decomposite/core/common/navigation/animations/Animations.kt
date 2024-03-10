package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.arkivanov.essenty.backhandler.BackEvent

fun softSpring() = spring(1.8f, 2500f, 0.0005f)

fun emptyAnimation() = contentAnimator(tween(0)) { Modifier }

fun fade(
    clean: Boolean = true,
    animateUsingGestures: Boolean = true,
    animationSpec: AnimationSpec<Float> = spring()
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
    minimumScale: Float = 0.9f,
    animationSpec: AnimationSpec<Float> = softSpring()
) = contentAnimator(animationSpec) {
    Modifier.graphicsLayer {
        val scale = (gestureAnimationProgress - (minimumScale * gestureAnimationProgress)).let {
            1f + (-it * it)
        }

        scaleX = scale
        scaleY = scale
    }
}

fun slide(
    orientation: Orientation = Orientation.Horizontal,
    targetOffsetDp: Int = 64,
    dependOnSwipeEdge: Boolean = false,
    animationSpec: AnimationSpec<Float> = softSpring(),
) = contentAnimator(animationSpec) {
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
    backStackSlideFraction: Float = 0.25f,
    animationSpec: AnimationSpec<Float> = softSpring()
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
    orientation: Orientation = Orientation.Horizontal,
    targetOffsetDp: Int = 64,
    animateFadeUsingGestures: Boolean = false,
    animationSpec: AnimationSpec<Float> = softSpring()
) = fade(
    clean = true,
    animateUsingGestures = animateFadeUsingGestures,
    animationSpec = animationSpec
) + slide(
    orientation = orientation,
    targetOffsetDp = targetOffsetDp,
    animationSpec = animationSpec
)




