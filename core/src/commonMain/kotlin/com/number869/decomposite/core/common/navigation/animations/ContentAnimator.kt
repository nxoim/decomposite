package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest

/**
 * In reality is just a holder for content that's to be animated, 📮
 */
@Immutable
class ContentAnimator(val animatedModifier: Modifier)

@Composable
fun NavigationItem.contentAnimator(
    animationSpec: AnimationSpec<Float> = softSpring(),
    block: ContentAnimatorScope.() -> Modifier
): ContentAnimator {
    val scope = remember { ContentAnimatorScope(this.index, this.indexFromTop, animationSpec) }
    // launch animations if there's changes
    LaunchedEffect(this.index, this.indexFromTop) {
        scope.updateCurrentIndexAndAnimate(index, indexFromTop)
    }

    LaunchedEffect(Unit) {
        val allowGestures = this@contentAnimator.index != 0 && scope.allowAnimation
        // trigger gestures in the animation scope
        if (allowGestures) sharedBackEventScope.gestureActions.collectLatest {
            scope.onBackGesture(it)
        }
    }

    LaunchedEffect(Unit) {
        scope.removalRequestChannel.collect { if (index <= -1) requestRemoval(it) }
    }
    return ContentAnimator(block(scope))
}

@Stable
@Composable
fun NavigationItem.LocalContentAnimator(): ProvidableCompositionLocal<ContentAnimator> {
    val anim = emptyAnimation()
    return staticCompositionLocalOf { anim }
}

//val LocalNavigationItem = staticCompositionLocalOf<NavigationItem> {
//    error("No NavigationItem provided locally")
//}

@Composable
fun NavigationItem.animatedDestination(
    animation: ContentAnimator = LocalContentAnimator().current,
    content: @Composable BoxScope.() -> Unit
) = Box(animation.animatedModifier, content = content)

/**
 * Usage of this operator will override the animation specifications of all animations with
 * the ones declared in the last animation. That is necessary because the animator scope
 * must be shared between the animations.
 */
operator fun ContentAnimator.plus(other: ContentAnimator) = ContentAnimator(
    this.animatedModifier.then(other.animatedModifier)
)