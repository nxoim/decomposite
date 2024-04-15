package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.nxoim.decomposite.core.common.navigation.animations.scopes.ContentAnimatorScope
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

@Stable
inline operator fun ContentAnimations.plus(other: ContentAnimations) = ContentAnimations(
    this.items + other.items
)