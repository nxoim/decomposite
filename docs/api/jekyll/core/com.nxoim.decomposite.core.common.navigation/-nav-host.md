---
title: NavHost
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation](index.html)/[NavHost](-nav-host.html)



# NavHost



[common]\




@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



fun &lt;[C](-nav-host.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [NavHost](-nav-host.html)(startingNavControllerInstance: [NavController](-nav-controller/index.html)&lt;[C](-nav-host.html)&gt;, modifier: [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html) = Modifier, excludedDestinations: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[C](-nav-host.html)&gt;? = null, animations: [DestinationAnimationsConfiguratorScope](../com.nxoim.decomposite.core.common.navigation.animations/-destination-animations-configurator-scope/index.html)&lt;[C](-nav-host.html)&gt;.() -&gt; [ContentAnimations](../com.nxoim.decomposite.core.common.navigation.animations/-content-animations/index.html) = LocalContentAnimator.current, router: @[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)[AnimatedVisibilityScope](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedVisibilityScope.html).(destination: [C](-nav-host.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))



Sets up stack animators for overlays and contained content and manages back gestures for the animations. Animations are passed down using [CompositionLocalProvider](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary.html) for other navigation hosts to consume.



[router](-nav-host.html) is a typical router where you declare the content of each destination.



#### Parameters


common

| | |
|---|---|
| excludedDestinations | allows to specify what destinations should not be rendered and animated. |




