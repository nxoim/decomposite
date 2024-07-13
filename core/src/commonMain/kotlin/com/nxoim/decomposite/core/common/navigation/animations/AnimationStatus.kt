package com.nxoim.decomposite.core.common.navigation.animations

import com.nxoim.decomposite.core.common.navigation.animations.AnimationType.Companion.gestures
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType.Companion.none
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType.Companion.passive
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType.Companion.passiveCancelling
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.inwards
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.none
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.outwards
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.back
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.outside
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.top

/**
 * Represents the status of an animation.
 *
 * [previousLocation] must represent the previous location of an item in the stack.
 * If the item has not been moved in the stack - [previousLocation] must be null.
 *
 * [location] must represent the current location of an item in the stack
 * (irrespective of the animation) and it's changes must trigger the animation.
 *
 * [direction] must represent the direction of the animation even when animating
 * with gestures.
 *
 * [animationType] must represent the type of the current animation.
 */
data class AnimationStatus(
	val previousLocation: ItemLocation?,
	val location: ItemLocation,
	val direction: Direction,
	val animationType: AnimationType
) {
	init {
		if ((direction.none && !animationType.none) || (!direction.none && animationType.none)) {
			error("direction must not be none if animation type is not none, and vice versa. Incorrect state: $this")
		}
	}
	val animating = !animationType.none && !direction.none

	/**
	 * This is true when the animation(gestures and passive, excluding the
	 * navigation cancellation animation) targets the top of the stack from the back.
	 *
	 * Example: removal of an item from the stack while this item was in the back
	 */
	val fromBackIntoTop
		get() = (location.indexFromTop == 1 && animationType.gestures)
				|| (location.top && previousLocation != null && previousLocation.back && direction.outwards && !animationType.passiveCancelling)

	/**
	 * This is true when the animation(passive, excluding the navigation cancellation
	 * animation) targets the back of the stack from the top.
	 *
	 * Example: addition of an item to the stack when this item was at the top
	 */
	val fromTopIntoBack
		get() = location.back
				&& previousLocation != null
				&& previousLocation.top
				&& direction.inwards
				&& !animationType.passiveCancelling

	/**
	 * This is true when the animation(gestures and passive, excluding the
	 * 	navigation cancellation animation) targets outside of the stack from the top.
	 *
	 * 	Example: removal of an item from the stack
	 */
	// "!location.back" instead of "location.top" because several items
	// can be removed consecutively and therefore have an indexFromTop
	// even less than -1, meaning an item can have previousLocation.outside
	// with location.outside, like backstack items can have previousLocation.back
	// with location.back
	val fromTopToOutside
		get() = (!location.back && animationType.gestures && direction.outwards)
				|| (location.outside && previousLocation != null && !previousLocation.back && direction.outwards && !animationType.passiveCancelling)

	/**
	 * This is true when the animation(passive, excluding the navigation cancellation
	 * animation) targets the top of the stack from outside.
	 *
	 * Example: addition of this item to stack.
	 */
	val fromOutsideIntoTop
		get() = location.top
				&& animationType.passive
				&& direction.inwards
				&& (previousLocation == null || previousLocation.outside)

	/**
	 * This is true when this item is going from a position in the back to another
	 * position in the back that is closer to the top, e.g. from 2 to
	 * 1 (0 being the top of the stack).
	 *
	 * Example: removal of an item from the stack while this item was (and will
	 * still be) in the back.
	 */
	val fromLowerBackIntoUpper
		get() = previousLocation != null
				&& previousLocation.indexFromTop > location.indexFromTop
				&& !animationType.passiveCancelling

	/**
	 * This is true when this item is going from a position in the back to another
	 * position in the back that is closer to the bottom, e.g. from 1 to
	 * 2 (0 being the top of the stack).
	 *
	 * Example: addition of an item to the stack when this item was (and will still
	 * be) in the back.
	 */
	val fromUpperBackIntoLower
		get() = previousLocation != null
				&& previousLocation.indexFromTop < location.indexFromTop
				&& !animationType.passiveCancelling

	/**
	 * This is true when this item is going from a position in the back to
	 * another position in the back in any order, e.g. from 2 to 1, or from
	 * 1 to 2 (0 being the top of the stack).
	 */
	val fromBackToBack = fromUpperBackIntoLower || fromLowerBackIntoUpper
}

/**
 * Represents the location of an item in the stack.
 * [indexFromTop] is the index of the item from the top of the stack, with 0 being the top.
 * Negative [indexFromTop] represents an item not existing in the stack.
 */
sealed class ItemLocation(val indexFromTop: Int) {
	data class Back(private val _indexFromTop: Int) : ItemLocation(_indexFromTop)
	data object Top : ItemLocation(0)
	data class Outside(private val _indexFromTop: Int) : ItemLocation(_indexFromTop)

	companion object {
		val ItemLocation.top get() = this == Top
		val ItemLocation.back get() = this is Back

		/**
		 * Only happens upon removal of an item from the stack
		 */
		val ItemLocation.outside get() = this is Outside
	}
}

/**
 * Represents the direction of an animation.
 */
enum class Direction {
	None, Inwards, Outwards;

	companion object {
		val Direction.inwards get() = this == Inwards
		val Direction.outwards get() = this == Outwards
		val Direction.none get() = this == None
	}
}

/**
 * Represents the type of an animation.
 */
enum class AnimationType {
	None, Gestures, Passive, PassiveCancelling;

	companion object {
		val AnimationType.gestures get() = this == Gestures
		val AnimationType.passive get() = this == Passive
		val AnimationType.passiveCancelling get() = this == PassiveCancelling
		val AnimationType.none get() = this == None
	}
}