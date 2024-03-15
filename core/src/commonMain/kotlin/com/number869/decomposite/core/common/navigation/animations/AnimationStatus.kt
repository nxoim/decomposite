package com.number869.decomposite.core.common.navigation.animations

interface AnimationStatus {
    val animating: Boolean
}

data class DefaultAnimationStatus(
    val previousLocation: ItemLocation,
    val location: ItemLocation,
    val direction: Direction,
    val type: AnimationType
) : AnimationStatus{
    override val animating = !type.none || !direction.none
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
val Direction.none get() = this == Direction.None

enum class AnimationType { None, Gestures, Passive }

val AnimationType.gestures get() = this == AnimationType.Gestures
val AnimationType.passive get() = this == AnimationType.Passive
val AnimationType.none get() = this == AnimationType.None