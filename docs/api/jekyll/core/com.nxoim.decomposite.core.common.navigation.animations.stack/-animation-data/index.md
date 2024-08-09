---
title: AnimationData
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.stack](../index.html)/[AnimationData](index.html)



# AnimationData



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



data class [AnimationData](index.html)(val scopes: () -&gt; [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html)&gt;, val modifiers: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html)&gt;, val renderUntils: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)&gt;, val requireVisibilityInBackstacks: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;)



## Constructors


| | |
|---|---|
| [AnimationData](-animation-data.html) | [common]<br>constructor(scopes: () -&gt; [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html)&gt;, modifiers: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html)&gt;, renderUntils: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)&gt;, requireVisibilityInBackstacks: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;) |


## Properties


| Name | Summary |
|---|---|
| [modifiers](modifiers.html) | [common]<br>val [modifiers](modifiers.html): () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html)&gt; |
| [renderUntils](render-untils.html) | [common]<br>val [renderUntils](render-untils.html): () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)&gt; |
| [requireVisibilityInBackstacks](require-visibility-in-backstacks.html) | [common]<br>val [requireVisibilityInBackstacks](require-visibility-in-backstacks.html): () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt; |
| [scopes](scopes.html) | [common]<br>val [scopes](scopes.html): () -&gt; [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html)&gt; |

