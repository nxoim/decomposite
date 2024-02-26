package com.number869.decomposite.core.common.navigation.animations

data class AnimationStatus(
    val previousLocation: ItemLocation,
    val location: ItemLocation,
    val direction: Direction,
    val type: AnimationType
) {
    val animating = type != AnimationType.None
}

enum class ItemLocation { Back, Top, Outside }

val ItemLocation.top get() = this == ItemLocation.Top

val ItemLocation.back get() = this == ItemLocation.Back

/**
 * Only happens upon removal of an item from the stack
 */
val ItemLocation.outside get() = this == ItemLocation.Outside

enum class Direction { None, Inward, Outward }
val Direction.inward get() = this == Direction.Inward
val Direction.outward get() = this == Direction.Outward

enum class AnimationType { None, Gestures, Passive }

val AnimationType.gestures get() = this == AnimationType.Gestures
val AnimationType.passive get() = this == AnimationType.Passive