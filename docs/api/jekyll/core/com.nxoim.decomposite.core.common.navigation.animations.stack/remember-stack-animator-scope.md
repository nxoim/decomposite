---
title: rememberStackAnimatorScope
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.stack](index.html)/[rememberStackAnimatorScope](remember-stack-animator-scope.html)



# rememberStackAnimatorScope



[common]\




@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



fun &lt;[Key](remember-stack-animator-scope.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [Instance](remember-stack-animator-scope.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [rememberStackAnimatorScope](remember-stack-animator-scope.html)(stack: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](remember-stack-animator-scope.html)&gt;, onBackstackChange: (stackEmpty: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), itemKey: ([Instance](remember-stack-animator-scope.html)) -&gt; [Key](remember-stack-animator-scope.html), excludedDestinations: ([Instance](remember-stack-animator-scope.html)) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), animations: [DestinationAnimationsConfiguratorScope](../com.nxoim.decomposite.core.common.navigation.animations/-destination-animations-configurator-scope/index.html)&lt;[Instance](remember-stack-animator-scope.html)&gt;.() -&gt; [ContentAnimations](../com.nxoim.decomposite.core.common.navigation.animations/-content-animations/index.html), allowBatchRemoval: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, animationDataRegistry: [AnimationDataRegistry](-animation-data-registry/index.html)&lt;[Key](remember-stack-animator-scope.html)&gt; = remember { AnimationDataRegistry() }): [StackAnimatorScope](-stack-animator-scope/index.html)&lt;[Key](remember-stack-animator-scope.html), [Instance](remember-stack-animator-scope.html)&gt;



Creates an instance of a [StackAnimatorScope](-stack-animator-scope/index.html). It manages instance caching and animations.




