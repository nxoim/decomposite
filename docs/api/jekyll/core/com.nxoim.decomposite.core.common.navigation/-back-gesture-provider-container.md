---
title: BackGestureProviderContainer
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation](index.html)/[BackGestureProviderContainer](-back-gesture-provider-container.html)



# BackGestureProviderContainer



[common]\




@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)



fun [BackGestureProviderContainer](-back-gesture-provider-container.html)(defaultComponentContext: DefaultComponentContext, modifier: [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html) = Modifier, startEdgeEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, endEdgeEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, edgeWidth: [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html)? = 16.dp, activationOffsetThreshold: [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html) = 4.dp, progressConfirmationThreshold: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) = 0.2f, velocityConfirmationThreshold: [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html) = 8.dp, blockChildDragInputs: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, content: @[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))



Handles back gestures on both edges of the screen and drives the provided BackDispatcher accordingly.



#### Parameters


common

| | |
|---|---|
| defaultComponentContext | is used for getting the back dispatcher conveniently. |
| modifier | a [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html) to applied to the overlay. |
| startEdgeEnabled | controls whether the start edge is enabled or not, left in LTR mode and right in RTL mode. |
| endEdgeEnabled | controls whether the end edge is enabled or not, right in LTR mode and left in RTL mode. |
| edgeWidth | the width in [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html) from the screen edge where the gesture first down touch is recognized. When null - use the entire width of the screen. |
| activationOffsetThreshold | a distance threshold in [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html) from the initial touch point in the direction of gesture. The gesture is initiated once this threshold is surpassed. |
| progressConfirmationThreshold | a threshold of progress that needs to be reached for the gesture to be confirmed once the touch is completed. The gesture is cancelled if the touch is completed without reaching the threshold. |
| content | a content to be shown under the overlay. |




