---
title: AnimationStatus
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations](../index.html)/[AnimationStatus](index.html)



# AnimationStatus



[common]\
data class [AnimationStatus](index.html)(val previousLocation: [ItemLocation](../-item-location/index.html)?, val location: [ItemLocation](../-item-location/index.html), val direction: [Direction](../-direction/index.html), val animationType: [AnimationType](../-animation-type/index.html))

Represents the status of an animation.



[previousLocation](previous-location.html) must represent the previous location of an item in the stack. If the item has not been moved in the stack - [previousLocation](previous-location.html) must be null.



[location](location.html) must represent the current location of an item in the stack (irrespective of the animation) and it's changes must trigger the animation.



[direction](direction.html) must represent the direction of the animation even when animating with gestures.



[animationType](animation-type.html) must represent the type of the current animation.



## Constructors


| | |
|---|---|
| [AnimationStatus](-animation-status.html) | [common]<br>constructor(previousLocation: [ItemLocation](../-item-location/index.html)?, location: [ItemLocation](../-item-location/index.html), direction: [Direction](../-direction/index.html), animationType: [AnimationType](../-animation-type/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [animating](animating.html) | [common]<br>val [animating](animating.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [animationType](animation-type.html) | [common]<br>val [animationType](animation-type.html): [AnimationType](../-animation-type/index.html) |
| [direction](direction.html) | [common]<br>val [direction](direction.html): [Direction](../-direction/index.html) |
| [fromBackIntoTop](from-back-into-top.html) | [common]<br>val [fromBackIntoTop](from-back-into-top.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when the animation(gestures and passive, excluding the navigation cancellation animation) targets the top of the stack from the back. |
| [fromBackToBack](from-back-to-back.html) | [common]<br>val [fromBackToBack](from-back-to-back.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when this item is going from a position in the back to another position in the back in any order, e.g. from 2 to 1, or from 1 to 2 (0 being the top of the stack). |
| [fromBackToOutside](from-back-to-outside.html) | [common]<br>val [fromBackToOutside](from-back-to-outside.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when the animation(passive, excluding the navigation cancellation animation) targets the outside of the stack from the back. |
| [fromLowerBackIntoUpper](from-lower-back-into-upper.html) | [common]<br>val [fromLowerBackIntoUpper](from-lower-back-into-upper.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when this item is going from a position in the back to another position in the back that is closer to the top, e.g. from 2 to 1 (0 being the top of the stack). |
| [fromOutsideIntoTop](from-outside-into-top.html) | [common]<br>val [fromOutsideIntoTop](from-outside-into-top.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when the animation(passive, excluding the navigation cancellation animation) targets the top of the stack from outside. |
| [fromTopIntoBack](from-top-into-back.html) | [common]<br>val [fromTopIntoBack](from-top-into-back.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when the animation(passive, excluding the navigation cancellation animation) targets the back of the stack from the top. |
| [fromTopToOutside](from-top-to-outside.html) | [common]<br>val [fromTopToOutside](from-top-to-outside.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when the animation(gestures and passive, excluding the navigation cancellation animation) targets outside of the stack from the top. |
| [fromUpperBackIntoLower](from-upper-back-into-lower.html) | [common]<br>val [fromUpperBackIntoLower](from-upper-back-into-lower.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>This is true when this item is going from a position in the back to another position in the back that is closer to the bottom, e.g. from 1 to 2 (0 being the top of the stack). |
| [location](location.html) | [common]<br>val [location](location.html): [ItemLocation](../-item-location/index.html) |
| [previousLocation](previous-location.html) | [common]<br>val [previousLocation](previous-location.html): [ItemLocation](../-item-location/index.html)? |
| [targetingBack](targeting-back.html) | [common]<br>val [targetingBack](targeting-back.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [targetingOutside](targeting-outside.html) | [common]<br>val [targetingOutside](targeting-outside.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [targetingTop](targeting-top.html) | [common]<br>val [targetingTop](targeting-top.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

