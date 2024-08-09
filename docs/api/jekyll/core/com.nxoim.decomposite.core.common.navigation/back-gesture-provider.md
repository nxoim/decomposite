---
title: backGestureProvider
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation](index.html)/[backGestureProvider](back-gesture-provider.html)



# backGestureProvider



[common]\
fun [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html).[backGestureProvider](back-gesture-provider.html)(backDispatcher: BackDispatcher, leftEdgeEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, rightEdgeEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, edgeWidth: [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html)? = null, activationOffsetThreshold: [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html) = 4.dp, progressConfirmationThreshold: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) = 0.2f, velocityConfirmationThreshold: [Dp](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/Dp.html) = 8.dp, blockChildDragInputs: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false): [Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier.html)



Detects drag gestures on a composable and dispatches back gestures to a BackDispatcher.



#### Parameters


common

| | |
|---|---|
| backDispatcher | The BackDispatcher instance that will receive the back gestures. |
| leftEdgeEnabled | Whether the left edge of the composable is enabled for back gestures. |
| rightEdgeEnabled | Whether the right edge of the composable is enabled for back gestures. |
| edgeWidth | The width of the edge area that will trigger a back gesture. Defaults to null. When null - uses the full composable's width |
| activationOffsetThreshold | The minimum distance the user must drag to activate a back gesture. Defaults to 4.dp. |
| progressConfirmationThreshold | The minimum progress required to confirm a back gesture. Defaults to 0.2F. |
| velocityConfirmationThreshold | The minimum velocity required to confirm a back gesture. Defaults to 8.dp. |
| blockChildDragInputs | Whether to block child drag inputs. Defaults to false. |




