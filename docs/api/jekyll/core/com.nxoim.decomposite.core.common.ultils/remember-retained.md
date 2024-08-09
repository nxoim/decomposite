---
title: rememberRetained
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.ultils](index.html)/[rememberRetained](remember-retained.html)



# rememberRetained



[common]\




@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)



@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



fun &lt;[T](remember-retained.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [rememberRetained](remember-retained.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)? = null, componentContext: ComponentContext = LocalComponentContext.current, block: @[DisallowComposableCalls](https://developer.android.com/reference/kotlin/androidx/compose/runtime/DisallowComposableCalls.html)() -&gt; [T](remember-retained.html)): [T](remember-retained.html)



Alternative to [rememberSaveable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/saveable/package-summary.html) that keeps the value alive during configuration changes, but not process death. Remembers and stores the value in the InstanceKeeper tied to ComponentContext provided by [LocalComponentContext](-local-component-context.html), meaning the value will be retained as long as the component/backstack entry (provided by [NavHost](../com.nxoim.decomposite.core.common.navigation/-nav-host.html)) exists.




