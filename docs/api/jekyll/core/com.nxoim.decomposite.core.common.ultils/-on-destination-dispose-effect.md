---
title: OnDestinationDisposeEffect
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.ultils](index.html)/[OnDestinationDisposeEffect](-on-destination-dispose-effect.html)



# OnDestinationDisposeEffect



[common]\




@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



fun [OnDestinationDisposeEffect](-on-destination-dispose-effect.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)? = null, waitForCompositionRemoval: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, componentContext: ComponentContext = LocalComponentContext.current, block: @[DisallowComposableCalls](https://developer.android.com/reference/kotlin/androidx/compose/runtime/DisallowComposableCalls.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))



Kind of like [DisposableEffect](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary.html), but relies on InstanceKeeper.Instance's onDestroy to make sure an action is done when a component is actually destroyed, surviving configuration changes. Keys must be unique.




