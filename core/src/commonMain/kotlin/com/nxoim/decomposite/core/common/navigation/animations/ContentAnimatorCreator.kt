package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.nxoim.decomposite.core.common.navigation.animations.scopes.ContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.scopes.contentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.stack.StackAnimator
import kotlin.jvm.JvmInline

/**
 * Contains all animations in a list.
 */
@JvmInline
@Immutable
value class ContentAnimations(val items: List<ContentAnimatorCreator<*>>)

/**
 * Represents the animator used by [StackAnimator] to create the animation scope.
 *
 * @param [key] helps optimize scope creation by preventing the creation
 * of a new scope if one with the same key already exists.
 *
 * @param [renderUntil] controls content rendering based on its position
 * in the stack, with 0 being the top. [StackAnimator] will not render an item if its
 * position is greater than [renderUntil]. The animation scope implementation
 * can be aware of [renderUntil].
 *
 * @param [requireVisibilityInBackstack] manages the item's visibility in the
 * backstack after animations are completed. If an item's position exceeds
 * [renderUntil] and [requireVisibilityInBackstack] is true, the item will remain visible.
 * Note that if multiple animations (e.g., fade() + scale()) are combined,
 * and at least one has [requireVisibilityInBackstack] set to true, all items
 * that do not meet [renderUntil] will be visible in the backstack.
 *
 * @param [animatorScopeFactory] is used to create the animation scope.
 * Refer to [contentAnimator] for an example.
 *
 * @param [animationModifier] provides the animated [Modifier] to the content.
 */
@Immutable
data class ContentAnimatorCreator<T : ContentAnimator>(
    val key: String,
    val renderUntil: Int,
    val requireVisibilityInBackstack: Boolean,
    val animatorScopeFactory: (
        initialIndex: Int,
        initialIndexFromTop: Int
    ) -> T,
    val animationModifier: (T) -> Modifier
)

@Stable
inline operator fun ContentAnimations.plus(
    other: ContentAnimations
) = ContentAnimations(this.items + other.items)

/**
 * Provides data helpful for the configuration of animations.
 */
@Immutable
data class DestinationAnimationsConfiguratorScope<T : Any>(
    val previousChild: T?,
    val currentChild: T,
    val nextChild: T?,
    val exitingChildren: () -> List<T>,
)

val LocalContentAnimator = staticCompositionLocalOf<DestinationAnimationsConfiguratorScope<*>.() -> ContentAnimations> {
    { cleanSlideAndFade() }
}