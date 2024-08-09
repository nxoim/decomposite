---
title: NavigationRootProvider
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation](index.html)/[NavigationRootProvider](-navigation-root-provider.html)



# NavigationRootProvider



[common]\




@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



fun [NavigationRootProvider](-navigation-root-provider.html)(navigationRootData: [NavigationRootData](-navigation-root-data/index.html), content: @[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))



Fallback implementation of a navigation root provider. Uses BoxWithConstraints to provide the screen size, which is possibly incorrect on some platforms. Please use a platform implementation. You are welcome to open an issue or PR.




