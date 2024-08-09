---
title: com.nxoim.decomposite.core.android.navigation
---
//[core](../../index.html)/[com.nxoim.decomposite.core.android.navigation](index.html)



# Package-level declarations



## Functions


| Name | Summary |
|---|---|
| [defaultNavigationRootData](default-navigation-root-data.html) | [android]<br>fun [ComponentActivity](https://developer.android.com/reference/kotlin/androidx/activity/ComponentActivity.html).[defaultNavigationRootData](default-navigation-root-data.html)(): [NavigationRootData](../com.nxoim.decomposite.core.common.navigation/-navigation-root-data/index.html)<br>Creates a default platform-specific instance of [NavigationRootData](../com.nxoim.decomposite.core.common.navigation/-navigation-root-data/index.html). |
| [NavigationRootProvider](-navigation-root-provider.html) | [android]<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>fun [NavigationRootProvider](-navigation-root-provider.html)(navigationRootData: [NavigationRootData](../com.nxoim.decomposite.core.common.navigation/-navigation-root-data/index.html), content: @[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Android specific navigation root provider. Collects the screen size and shape for animations. Uses [CommonNavigationRootProvider](../com.nxoim.decomposite.core.common.navigation/-common-navigation-root-provider.html). |

