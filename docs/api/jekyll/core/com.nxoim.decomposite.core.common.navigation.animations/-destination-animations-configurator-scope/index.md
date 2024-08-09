---
title: DestinationAnimationsConfiguratorScope
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations](../index.html)/[DestinationAnimationsConfiguratorScope](index.html)



# DestinationAnimationsConfiguratorScope



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



data class [DestinationAnimationsConfiguratorScope](index.html)&lt;[T](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;(val previousChild: [T](index.html)?, val currentChild: [T](index.html), val nextChild: [T](index.html)?, val exitingChildren: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](index.html)&gt;)

Provides data helpful for the configuration of animations.



## Constructors


| | |
|---|---|
| [DestinationAnimationsConfiguratorScope](-destination-animations-configurator-scope.html) | [common]<br>constructor(previousChild: [T](index.html)?, currentChild: [T](index.html), nextChild: [T](index.html)?, exitingChildren: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](index.html)&gt;) |


## Properties


| Name | Summary |
|---|---|
| [currentChild](current-child.html) | [common]<br>val [currentChild](current-child.html): [T](index.html) |
| [exitingChildren](exiting-children.html) | [common]<br>val [exitingChildren](exiting-children.html): () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](index.html)&gt; |
| [nextChild](next-child.html) | [common]<br>val [nextChild](next-child.html): [T](index.html)? |
| [previousChild](previous-child.html) | [common]<br>val [previousChild](previous-child.html): [T](index.html)? |

