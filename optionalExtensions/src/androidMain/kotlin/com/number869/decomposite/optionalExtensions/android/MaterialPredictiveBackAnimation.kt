@file:OptIn(ExperimentalDecomposeApi::class)

package com.number869.decomposite.optionalExtensions.android

import android.content.Context
import android.os.Build
import android.view.RoundedCorner
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.*
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Deprecated("Use Decompose's default predictive back animation", ReplaceWith(
    "predictiveBackAnimation(backHandler, onBack)",
    "import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation\n"
)
)
@OptIn(ExperimentalDecomposeApi::class)
fun <C : Any, T : Any> materialPredictiveBackAnimation(
    backHandler: BackHandler,
    animation: StackAnimation<C, T> = stackAnimation(fade() + scale()),
    onBack: () -> Unit,
    cornerRadius: Dp = 16.dp,
    selector: (
        initialBackEvent: BackEvent,
        exitChild: Child.Created<C, T>,
        enterChild: Child.Created<C, T>
    ) -> PredictiveBackAnimatable = { initialBackEvent, exitChild, enterChild ->
        MaterialPredictiveBackAnimatable(initialBackEvent, cornerRadius)
    },
): StackAnimation<C, T> = predictiveBackAnimation(backHandler, animation, selector, onBack)

@OptIn(ExperimentalDecomposeApi::class)
private class MaterialPredictiveBackAnimatable(
    private val initialEvent: BackEvent,
    private val cornerRadius: Dp
) : PredictiveBackAnimatable {
    private val finishProgressAnimatable = Animatable(initialValue = 1F)
    private val finishProgress by derivedStateOf { finishProgressAnimatable.value }
    private val progressAnimatable = Animatable(initialValue = initialEvent.progress)
    private val progress by derivedStateOf { progressAnimatable.value }
    private var edge by mutableStateOf(initialEvent.swipeEdge)
    private var touchY by mutableFloatStateOf(initialEvent.touchY)

    override val exitModifier: Modifier
        @RequiresApi(Build.VERSION_CODES.S)
        get() = Modifier.composed {
            val context = LocalContext.current
            val windowInsets = LocalView.current.rootWindowInsets
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val screen = wm.maximumWindowMetrics.bounds
            val actualScreenSize = Size(screen.height().toFloat(), screen.width().toFloat())
            val devicesCornerRadius = windowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius

            graphicsLayer {
                val layerIsFullscreen = actualScreenSize == this.size

                val layerCornerRadius = if (layerIsFullscreen && devicesCornerRadius != null) {
                    (devicesCornerRadius / density).dp
                } else {
                    cornerRadius * progress
                }

                transformOrigin = TransformOrigin(
                    pivotFractionX = when (edge) {
                        BackEvent.SwipeEdge.LEFT -> 1F
                        BackEvent.SwipeEdge.RIGHT -> 0F
                        BackEvent.SwipeEdge.UNKNOWN -> 0.5F
                    },
                    pivotFractionY = 0.5F
                )

                val scale = 1F - progress / 10F
                scaleX = scale
                scaleY = scale

                val translationXLimit = when (edge) {
                    BackEvent.SwipeEdge.LEFT -> -8.dp.toPx()
                    BackEvent.SwipeEdge.RIGHT -> 8.dp.toPx()
                    BackEvent.SwipeEdge.UNKNOWN -> 0F
                }
                translationX = translationXLimit * progress

                val translationYLimit = size.height / 20F - 8.dp.toPx()
                val translationYFactor = ((touchY - initialEvent.touchY) / size.height) * (progress * 3F).coerceAtMost(1f)
                translationY = translationYLimit * translationYFactor

                alpha = finishProgress
                shape = RoundedCornerShape(layerCornerRadius)
                clip = true
            }
        }

    override val enterModifier: Modifier
        get() =
            Modifier.drawWithContent {
                drawContent()
                drawRect(color = Color.Black.copy(alpha = finishProgress * 0.25F))
            }

    override suspend fun animate(event: BackEvent) {
        edge = event.swipeEdge
        touchY = event.touchY
        progressAnimatable.animateTo(event.progress)
    }

    override suspend fun finish() {
        val velocityFactor = progressAnimatable.velocity.coerceAtMost(1F) / 1F
        val progress = progressAnimatable.value
        coroutineScope {
            launch { progressAnimatable.animateTo(progress + (1F - progress) * velocityFactor) }
            launch { finishProgressAnimatable.animateTo(targetValue = 0F, animationSpec = tween()) }
        }
    }
}