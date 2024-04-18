package com.nxoim.decomposite.core.common.navigation

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackEvent.SwipeEdge
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


// based on PredictiveBackGestureOverlay from the decompose extensions
/**
 * Handles back gestures on both edges of the screen and drives the provided [BackDispatcher] accordingly.
 *
 * @param defaultComponentContext is used for getting the back dispatcher conveniently.
 * @param modifier a [Modifier] to applied to the overlay.
 * @param startEdgeEnabled controls whether the start edge is enabled or not,
 * left in LTR mode and right in RTL mode.
 * @param endEdgeEnabled controls whether the end edge is enabled or not,
 * right in LTR mode and left in RTL mode.
 * @param edgeWidth the width in [Dp] from the screen edge where the gesture first down touch is recognized. When null - use the entire width of the screen.
 * @param activationOffsetThreshold a distance threshold in [Dp] from the initial touch point in the direction
 * of gesture. The gesture is initiated once this threshold is surpassed.
 * @param progressConfirmationThreshold a threshold of progress that needs to be reached for the gesture
 * to be confirmed once the touch is completed. The gesture is cancelled if the touch is completed without
 * reaching the threshold.
 * @param content a content to be shown under the overlay.
 */
@ExperimentalDecomposeApi
@Composable
fun BackGestureProviderContainer(
    defaultComponentContext: DefaultComponentContext,
    modifier: Modifier = Modifier,
    startEdgeEnabled: Boolean = true,
    endEdgeEnabled: Boolean = false,
    edgeWidth: Dp? = null,
    activationOffsetThreshold: Dp = 16.dp,
    progressConfirmationThreshold: Float = 0.2F,
    velocityConfirmationThreshold: Dp = 4.dp,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier.backGestureProvider(
            backDispatcher = defaultComponentContext.backHandler as BackDispatcher,
            leftEdgeEnabled = when (layoutDirection) {
                LayoutDirection.Ltr -> startEdgeEnabled
                LayoutDirection.Rtl -> endEdgeEnabled
            },
            rightEdgeEnabled = when (layoutDirection) {
                LayoutDirection.Ltr -> endEdgeEnabled
                LayoutDirection.Rtl -> startEdgeEnabled
            },
            edgeWidth = edgeWidth,
            activationOffsetThreshold = activationOffsetThreshold,
            progressConfirmationThreshold = progressConfirmationThreshold,
            velocityConfirmationThreshold = velocityConfirmationThreshold
        )
    ) {
        content()
    }

    DisposableEffect(defaultComponentContext.backHandler) {
        val callback = BackCallback(priority = BackCallback.PRIORITY_MIN, onBack = {  })
        defaultComponentContext.backHandler.register(callback)
        onDispose { defaultComponentContext.backHandler.unregister(callback) }
    }
}

/**
 * Manipulates the back dispatcher. When [edgeWidth] is null - the entire composable's width
 * is used.
 */
@Stable
fun Modifier.backGestureProvider(
    backDispatcher: BackDispatcher,
    leftEdgeEnabled: Boolean = true,
    rightEdgeEnabled: Boolean = false,
    edgeWidth: Dp? = null, // if null - use max size
    activationOffsetThreshold: Dp = 16.dp,
    progressConfirmationThreshold: Float = 0.2F,
    velocityConfirmationThreshold: Dp = 4.dp,
) = pointerInput(backDispatcher, leftEdgeEnabled, rightEdgeEnabled) {
    val triggerWidth = edgeWidth?.let { it.value * density } ?: size.width.toFloat()
    var edge: SwipeEdge = SwipeEdge.UNKNOWN
    var progress = 0f
    var velocityPx = 0f
    var totalDistanceSwipedPx = 0f

    var dispatchingGestures: Boolean? = null

    coroutineScope {
        detectHorizontalDragGestures(
            onDragStart = { offset ->
                edge = when {
                    !rightEdgeEnabled -> SwipeEdge.LEFT
                    !leftEdgeEnabled -> SwipeEdge.RIGHT
                    else -> if (offset.x < (size.width / 2)) SwipeEdge.LEFT else SwipeEdge.RIGHT
                }

                // reset swipe state variables.
                totalDistanceSwipedPx = 0f
                progress = 0f
                velocityPx = 0f
                dispatchingGestures = null

                // check if we should dispatch gestures based on the starting position and enabled edges.
                dispatchingGestures = if (edge == SwipeEdge.LEFT) {
                    if (leftEdgeEnabled)
                        offset.x <= triggerWidth
                    else
                        return@detectHorizontalDragGestures
                } else {
                    if (rightEdgeEnabled)
                        size.width - offset.x <= triggerWidth
                    else
                        return@detectHorizontalDragGestures
                }

                if (dispatchingGestures == true) {
                    backDispatcher.startPredictiveBack(
                        BackEvent(
                            progress = progress.coerceIn(0f, 1f),
                            swipeEdge = edge,
                            touchX = offset.x,
                            touchY = offset.y
                        )
                    )
                }
            },
            onHorizontalDrag = { change, _ ->
                velocityPx = if (edge == SwipeEdge.LEFT) {
                    change.positionChange().x
                } else {
                    -change.positionChange().x
                }

                // stop dispatching gestures if the threshold is exceeded in the wrong direction.
                if (totalDistanceSwipedPx < 0) dispatchingGestures = false

                if (dispatchingGestures == true) {
                    if (totalDistanceSwipedPx < activationOffsetThreshold.value * density) {
                        totalDistanceSwipedPx += velocityPx
                    } else {
                        progress += (velocityPx / size.width)

                        backDispatcher.progressPredictiveBack(
                            BackEvent(
                                progress = progress.coerceIn(0f, 1f),
                                swipeEdge = edge,
                                touchX = change.position.x,
                                touchY = change.position.y
                            )
                        )
                    }
                }
            },
            onDragEnd = {
                if (dispatchingGestures == true) {
                    val velocityThresholdMet = velocityPx / density >= velocityConfirmationThreshold.value
                    val progressThresholdMet = progress >= progressConfirmationThreshold

                    if (velocityThresholdMet || progressThresholdMet)
                        backDispatcher.back()
                    else
                        backDispatcher.cancelPredictiveBack()
                }
            },
            onDragCancel = {
                if (dispatchingGestures == true) {
                    backDispatcher.cancelPredictiveBack()

                    launch {
                        animate(progress, 0f) { value, _ -> progress = value }
                    }
                }
            }
        )
    }
}


