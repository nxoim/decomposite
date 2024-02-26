package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import com.number869.decomposite.core.common.ultils.rememberRetained
import kotlinx.coroutines.flow.collectLatest

/**
 * In reality is just a holder for content that's to be animated, 📮
 */
@Immutable
class ContentAnimator(val content: @Composable ContentAnimatorScope.(@Composable () -> Unit) -> Unit)

fun contentAnimator(
    animationSpec: AnimationSpec<Float> = softSpring(),
    block: @Composable ContentAnimatorScope.(content: @Composable () -> Unit) -> Unit
) = ContentAnimator { block(this.apply { this.animationSpec = animationSpec }, it) }

val LocalContentAnimator = staticCompositionLocalOf { emptyAnimation() }

@Composable
fun NavigationItem.animatedDestination(
    animation: ContentAnimator = LocalContentAnimator.current,
    content: @Composable () -> Unit
) {
    val scope = rememberRetained { ContentAnimatorScope(this.index, this.indexFromTop) }

    animation.content(scope, content)

    // launch animations if there's changes
    LaunchedEffect(this.index, this.indexFromTop) {
        scope.updateCurrentIndex(index, indexFromTop)
        scope.animateToTarget()
    }

    LaunchedEffect(Unit) {
        // trigger gestures in the animation scope
         sharedBackEventScope.gestureActions.collectLatest {
            scope.onBackGesture(it)
        }
    }

    LaunchedEffect(Unit) {
        scope.removalRequestChannel.collect { if (index <= -1) requestRemoval(it) }
    }
}

operator fun ContentAnimator.plus(other: ContentAnimator) = ContentAnimator {
    this.content { other.content(this) { it() } }
}