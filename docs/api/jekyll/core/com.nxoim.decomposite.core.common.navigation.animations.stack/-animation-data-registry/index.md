---
title: AnimationDataRegistry
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.stack](../index.html)/[AnimationDataRegistry](index.html)



# AnimationDataRegistry



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [AnimationDataRegistry](index.html)&lt;[Key](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;



## Constructors


| | |
|---|---|
| [AnimationDataRegistry](-animation-data-registry.html) | [common]<br>constructor() |


## Functions


| Name | Summary |
|---|---|
| [forEach](for-each.html) | [common]<br>fun [forEach](for-each.html)(block: ([Map.Entry](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/-entry/index.html)&lt;[Key](index.html), [AnimationData](../-animation-data/index.html)&gt;) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)) |
| [getOrCreateAnimationData](get-or-create-animation-data.html) | [common]<br>fun [getOrCreateAnimationData](get-or-create-animation-data.html)(key: [Key](index.html), source: [ContentAnimations](../../com.nxoim.decomposite.core.common.navigation.animations/-content-animations/index.html), initialIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), initialIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [AnimationData](../-animation-data/index.html) |
| [remove](remove.html) | [common]<br>fun [remove](remove.html)(key: [Key](index.html)) |

