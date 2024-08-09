---
title: com.nxoim.decomposite.core.common.ultils
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.ultils](index.html)



# Package-level declarations



## Types


| Name | Summary |
|---|---|
| [BackGestureEvent](-back-gesture-event/index.html) | [common]<br>interface [BackGestureEvent](-back-gesture-event/index.html) |
| [ScreenInformation](-screen-information/index.html) | [common]<br>@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)<br>data class [ScreenInformation](-screen-information/index.html)(val widthPx: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val heightPx: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val screenShape: [ScreenShape](-screen-shape/index.html)) |
| [ScreenShape](-screen-shape/index.html) | [common]<br>@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)<br>data class [ScreenShape](-screen-shape/index.html)(val path: [Path](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/Path.html)?, val corners: [ScreenShapeCorners](-screen-shape-corners/index.html)?) |
| [ScreenShapeCorners](-screen-shape-corners/index.html) | [common]<br>@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)<br>data class [ScreenShapeCorners](-screen-shape-corners/index.html)(val topLeftPx: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val topRightPx: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val bottomLeftPx: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val bottomRightPx: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [LocalBackDispatcher](-local-back-dispatcher.html) | [common]<br>val [LocalBackDispatcher](-local-back-dispatcher.html): [ProvidableCompositionLocal](https://developer.android.com/reference/kotlin/androidx/compose/runtime/ProvidableCompositionLocal.html)&lt;BackDispatcher&gt; |
| [LocalComponentContext](-local-component-context.html) | [common]<br>val [LocalComponentContext](-local-component-context.html): [ProvidableCompositionLocal](https://developer.android.com/reference/kotlin/androidx/compose/runtime/ProvidableCompositionLocal.html)&lt;ComponentContext&gt; |


## Functions


| Name | Summary |
|---|---|
| [BackGestureHandler](-back-gesture-handler.html) | [common]<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>fun [BackGestureHandler](-back-gesture-handler.html)(enabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, backHandler: BackHandler = LocalComponentContext.current.backHandler, onBackStarted: (BackEvent) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = {}, onBackProgressed: (BackEvent) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = {}, onBackCancelled: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = {}, onBack: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Handles gestures using the provided back handler, which by default is the chileren's one. |
| [InOverlay](-in-overlay.html) | [common]<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>fun [InOverlay](-in-overlay.html)(content: @[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Displays content in the overlay provided [NavigationRootProvider](../com.nxoim.decomposite.core.common.navigation/-navigation-root-provider.html) |
| [noRippleClickable](no-ripple-clickable.html) | [common]<br>@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)<br>fun [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html).[noRippleClickable](no-ripple-clickable.html)(onClick: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = {}): [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html) |
| [OnDestinationDisposeEffect](-on-destination-dispose-effect.html) | [common]<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>fun [OnDestinationDisposeEffect](-on-destination-dispose-effect.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)? = null, waitForCompositionRemoval: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, componentContext: ComponentContext = LocalComponentContext.current, block: @[DisallowComposableCalls](https://developer.android.com/reference/kotlin/androidx/compose/runtime/DisallowComposableCalls.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Kind of like [DisposableEffect](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary.html), but relies on InstanceKeeper.Instance's onDestroy to make sure an action is done when a component is actually destroyed, surviving configuration changes. Keys must be unique. |
| [onDestroy](on-destroy.html) | [common]<br>fun ComponentContext.[onDestroy](on-destroy.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), block: @[DisallowComposableCalls](https://developer.android.com/reference/kotlin/androidx/compose/runtime/DisallowComposableCalls.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Saves the call in a container that executes the call before getting fully destroyed, surviving configuration changes, making sure the block is only executed when a component fully dies, |
| [rememberRetained](remember-retained.html) | [common]<br>@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>fun &lt;[T](remember-retained.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [rememberRetained](remember-retained.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)? = null, componentContext: ComponentContext = LocalComponentContext.current, block: @[DisallowComposableCalls](https://developer.android.com/reference/kotlin/androidx/compose/runtime/DisallowComposableCalls.html)() -&gt; [T](remember-retained.html)): [T](remember-retained.html)<br>Alternative to [rememberSaveable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/saveable/package-summary.html) that keeps the value alive during configuration changes, but not process death. Remembers and stores the value in the InstanceKeeper tied to ComponentContext provided by [LocalComponentContext](-local-component-context.html), meaning the value will be retained as long as the component/backstack entry (provided by [NavHost](../com.nxoim.decomposite.core.common.navigation/-nav-host.html)) exists. |

