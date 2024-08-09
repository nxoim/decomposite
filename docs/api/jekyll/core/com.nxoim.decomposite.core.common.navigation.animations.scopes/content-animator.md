---
title: contentAnimator
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.scopes](index.html)/[contentAnimator](content-animator.html)



# contentAnimator



[common]\
fun [contentAnimator](content-animator.html)(animationSpec: [AnimationSpec](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/AnimationSpec.html)&lt;[Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)&gt; = softSpring(), renderUntil: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1, requireVisibilityInBackstack: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, block: [DefaultContentAnimatorScope](-default-content-animator-scope/index.html).() -&gt; [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html)): [ContentAnimations](../com.nxoim.decomposite.core.common.navigation.animations/-content-animations/index.html)



Creates an animation scope.



[renderUntil](content-animator.html) Controls content rendering based on it's position in the stack and animation state. Content at or above [renderUntil](content-animator.html) is always rendered if it's the item is index 0 or -1 (top or outside).



If [requireVisibilityInBackstack](content-animator.html) is false (which is by default) - the top and outside items are rendered at all times while the backstack items are only rendered if they're being animated.



If [requireVisibilityInBackstack](content-animator.html) is set to false - will be visible even when it's not animated (note that if you're combining animations, like fade() + scale(), if one of them has [requireVisibilityInBackstack](content-animator.html) set to false - ALL items will be visible while in backstack as if all animations have [requireVisibilityInBackstack](content-animator.html) set to true).




