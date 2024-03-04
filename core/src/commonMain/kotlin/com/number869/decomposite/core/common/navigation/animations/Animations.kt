package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.arkivanov.essenty.backhandler.BackEvent

fun <T> softSpring() = spring<T>(1.8f, 2500f)

@Composable
fun NavigationItem.emptyAnimation() = contentAnimator(tween(0)) { Modifier }

@Composable
fun NavigationItem.fade(
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

@Composable
fun NavigationItem.scale(
    minimumScale: Float = 0.9f,
    rotate: Boolean = true,
    animationSpec: AnimationSpec<Float> = softSpring()
) = contentAnimator(animationSpec) {
    Modifier.graphicsLayer {
        val scale = (gestureAnimationProgress - (minimumScale * gestureAnimationProgress)).let {
            if (rotate) 1f + (-it * it) else it
        }

        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun NavigationItem.slide(
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

@Composable
fun NavigationItem.iosLikeSlide(
    backStackSlideFraction: Float = 0.25f,
    animationSpec: AnimationSpec<Float> = softSpring()
) = contentAnimator(animationSpec) {
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

@Composable
fun NavigationItem.cleanSlideAndFade(
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




