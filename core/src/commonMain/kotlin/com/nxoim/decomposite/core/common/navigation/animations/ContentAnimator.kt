package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import kotlin.jvm.JvmInline

@JvmInline
@Immutable
value class ContentAnimations(val items: List<ContentAnimator<*>>)

/**
 * Describes the animator and creates a scope. [key] is used to identify the scopes and
 * minimize their creation, as scopes with the same animator type and key will always have
 * 1 instance only.
 */
@Immutable
data class ContentAnimator<T : ContentAnimatorScope>(
    val key: String,
    val renderUntil: Int,
    val requireVisibilityInBackstack: Boolean,
    val animatorScopeFactory: (
        initialIndex: Int,
        initialIndexFromTop: Int
    ) -> T,
    val animationModifier: T.() -> Modifier
)

/**
 * [renderUntil] Controls content rendering based on it's position in the stack and animation state.
 * Content at or above [renderUntil] is always rendered if it's the item is index 0 or -1 (top or outside).
 *
 * If [requireVisibilityInBackstack] is false (which is by default) - the top and outside items
 * are rendered at all times while the backstack items are only rendered if they're being animated.
 *
 * If [requireVisibilityInBackstack] is set to false - will be visible even when it's not animated
 * (note that if you're combining animations, like fade() + scale(), if one of them has [requireVisibilityInBackstack]
 * set to false - ALL items will be visible while in backstack as if all animations have [requireVisibilityInBackstack]
 * set to true).
 */
@OptIn(InternalDecomposeApi::class)
fun contentAnimator(
    animationSpec: AnimationSpec<Float> = softSpring(),
    renderUntil: Int = 1,
    requireVisibilityInBackstack: Boolean = false,
    block: DefaultContentAnimatorScope.() -> Modifier
) = ContentAnimations(
    listOf(
        ContentAnimator(
            key = animationSpec.hashString(), // 1 instance per animation spec
            renderUntil = renderUntil,
            requireVisibilityInBackstack = requireVisibilityInBackstack,
            animatorScopeFactory = { initialIndex, initialIndexFromTop ->
                DefaultContentAnimatorScope(initialIndex, initialIndexFromTop, animationSpec)
            },
            animationModifier = block
        )
    )
)

@Stable
inline operator fun ContentAnimations.plus(other: ContentAnimations) = ContentAnimations(
    this.items + other.items
)