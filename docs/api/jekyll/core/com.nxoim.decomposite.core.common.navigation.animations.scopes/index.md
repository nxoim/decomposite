---
title: com.nxoim.decomposite.core.common.navigation.animations.scopes
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.scopes](index.html)



# Package-level declarations



## Types


| Name | Summary |
|---|---|
| [ContentAnimatorScope](-content-animator-scope/index.html) | [common]<br>interface [ContentAnimatorScope](-content-animator-scope/index.html)<br>Base for the content animator scope implementations. Describes the bare minimum needed. When implementing - keep in mind: |
| [DefaultContentAnimatorScope](-default-content-animator-scope/index.html) | [common]<br>@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)<br>class [DefaultContentAnimatorScope](-default-content-animator-scope/index.html)(initialIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), initialIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), animationSpec: [AnimationSpec](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/AnimationSpec.html)&lt;[Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)&gt;) : [ContentAnimatorScope](-content-animator-scope/index.html) |


## Functions


| Name | Summary |
|---|---|
| [contentAnimator](content-animator.html) | [common]<br>fun [contentAnimator](content-animator.html)(animationSpec: [AnimationSpec](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/AnimationSpec.html)&lt;[Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)&gt; = softSpring(), renderUntil: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1, requireVisibilityInBackstack: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, block: [DefaultContentAnimatorScope](-default-content-animator-scope/index.html).() -&gt; [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html)): [ContentAnimations](../com.nxoim.decomposite.core.common.navigation.animations/-content-animations/index.html)<br>Creates an animation scope. |

