---
title: ContentAnimator
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations](../index.html)/[ContentAnimator](index.html)



# ContentAnimator



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



data class [ContentAnimator](index.html)(val key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val renderUntil: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val requireVisibilityInBackstack: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), val animatorScopeFactory: (initialIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), initialIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) -&gt; [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html), val animationModifier: [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html).() -&gt; [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html))

Represents the animator used by [StackAnimator](../../com.nxoim.decomposite.core.common.navigation.animations.stack/-stack-animator.html) to create the animation scope.



The [key](key.html) parameter helps optimize scope creation by preventing the creation of a new scope if one with the same key already exists.



The [renderUntil](render-until.html) parameter controls content rendering based on its position in the stack, with 0 being the top. [StackAnimator](../../com.nxoim.decomposite.core.common.navigation.animations.stack/-stack-animator.html) will not render an item if its position is greater than [renderUntil](render-until.html). The animation scope implementation can be aware of [renderUntil](render-until.html).



The [requireVisibilityInBackstack](require-visibility-in-backstack.html) parameter manages the item's visibility in the backstack after animations are completed. If an item's position exceeds [renderUntil](render-until.html) and [requireVisibilityInBackstack](require-visibility-in-backstack.html) is true, the item will remain visible. Note that if multiple animations (e.g., fade() + scale()) are combined, and at least one has [requireVisibilityInBackstack](require-visibility-in-backstack.html) set to true, all items that do not meet [renderUntil](render-until.html) will be visible in the backstack.



The [animatorScopeFactory](animator-scope-factory.html) parameter is used to create the animation scope. Refer to [contentAnimator](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/content-animator.html) for an example.



The [animationModifier](animation-modifier.html) parameter provides the animated [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html) to the content.



## Constructors


| | |
|---|---|
| [ContentAnimator](-content-animator.html) | [common]<br>constructor(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), renderUntil: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), requireVisibilityInBackstack: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), animatorScopeFactory: (initialIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), initialIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) -&gt; [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html), animationModifier: [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html).() -&gt; [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html)) |


## Properties


| Name | Summary |
|---|---|
| [animationModifier](animation-modifier.html) | [common]<br>val [animationModifier](animation-modifier.html): [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html).() -&gt; [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html) |
| [animatorScopeFactory](animator-scope-factory.html) | [common]<br>val [animatorScopeFactory](animator-scope-factory.html): (initialIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), initialIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) -&gt; [ContentAnimatorScope](../../com.nxoim.decomposite.core.common.navigation.animations.scopes/-content-animator-scope/index.html) |
| [key](key.html) | [common]<br>val [key](key.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [renderUntil](render-until.html) | [common]<br>val [renderUntil](render-until.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [requireVisibilityInBackstack](require-visibility-in-backstack.html) | [common]<br>val [requireVisibilityInBackstack](require-visibility-in-backstack.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

