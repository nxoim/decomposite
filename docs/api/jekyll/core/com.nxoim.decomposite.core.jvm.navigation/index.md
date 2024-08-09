---
title: com.nxoim.decomposite.core.jvm.navigation
---
//[core](../../index.html)/[com.nxoim.decomposite.core.jvm.navigation](index.html)



# Package-level declarations



## Functions


| Name | Summary |
|---|---|
| [defaultNavigationRootData](default-navigation-root-data.html) | [jvm]<br>fun [defaultNavigationRootData](default-navigation-root-data.html)(): [NavigationRootData](../com.nxoim.decomposite.core.common.navigation/-navigation-root-data/index.html)<br>Creates a default platform-specific instance of [NavigationRootData](../com.nxoim.decomposite.core.common.navigation/-navigation-root-data/index.html). |
| [NavigationRootProvider](-navigation-root-provider.html) | [jvm]<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>fun [FrameWindowScope](https://developer.android.com/reference/kotlin/androidx/compose/ui/window/FrameWindowScope.html).[NavigationRootProvider](-navigation-root-provider.html)(navigationRootData: [NavigationRootData](../com.nxoim.decomposite.core.common.navigation/-navigation-root-data/index.html), content: @[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>JVM specific navigation root provider. Collects the max window size for animations. Uses [CommonNavigationRootProvider](../com.nxoim.decomposite.core.common.navigation/-common-navigation-root-provider.html). |

