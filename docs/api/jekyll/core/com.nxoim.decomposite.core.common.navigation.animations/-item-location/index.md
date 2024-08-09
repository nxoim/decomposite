---
title: ItemLocation
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations](../index.html)/[ItemLocation](index.html)



# ItemLocation

sealed class [ItemLocation](index.html)

Represents the location of an item in the stack. [indexFromTop](index-from-top.html) is the index of the item from the top of the stack, with 0 being the top. Negative [indexFromTop](index-from-top.html) represents an item not existing in the stack.



#### Inheritors


| |
|---|
| [Back](-back/index.html) |
| [Top](-top/index.html) |
| [Outside](-outside/index.html) |


## Types


| Name | Summary |
|---|---|
| [Back](-back/index.html) | [common]<br>data class [Back](-back/index.html)(_indexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [ItemLocation](index.html) |
| [Companion](-companion/index.html) | [common]<br>object [Companion](-companion/index.html) |
| [Outside](-outside/index.html) | [common]<br>data class [Outside](-outside/index.html)(_indexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [ItemLocation](index.html) |
| [Top](-top/index.html) | [common]<br>data object [Top](-top/index.html) : [ItemLocation](index.html) |


## Properties


| Name | Summary |
|---|---|
| [back](-companion/back.html) | [common]<br>val [ItemLocation](index.html).[back](-companion/back.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [indexFromTop](index-from-top.html) | [common]<br>val [indexFromTop](index-from-top.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [outside](-companion/outside.html) | [common]<br>val [ItemLocation](index.html).[outside](-companion/outside.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Only happens upon removal of an item from the stack |
| [top](-companion/top.html) | [common]<br>val [ItemLocation](index.html).[top](-companion/top.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

