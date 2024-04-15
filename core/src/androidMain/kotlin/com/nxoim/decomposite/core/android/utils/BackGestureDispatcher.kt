package com.nxoim.decomposite.core.android.utils

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackEvent.SwipeEdge

fun ComponentActivity.backGestureDispatcher(): BackDispatcher {
    val dispatcher = BackDispatcher()

    val callback = object : OnBackPressedCallback(enabled = false) {
        @RequiresApi(34)
        override fun handleOnBackStarted(backEvent: BackEventCompat) {
            val edge = if (backEvent.swipeEdge == 0) SwipeEdge.LEFT else SwipeEdge.RIGHT

            dispatcher.startPredictiveBack(
                com.arkivanov.essenty.backhandler.BackEvent(
                    progress = backEvent.progress,
                    swipeEdge = edge,
                    touchX = backEvent.touchX,
                    touchY = backEvent.touchY
                )
            )
        }

        @RequiresApi(34)
        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            val edge = if (backEvent.swipeEdge == 0) SwipeEdge.LEFT else SwipeEdge.RIGHT

            dispatcher.progressPredictiveBack(
                com.arkivanov.essenty.backhandler.BackEvent(
                    progress = backEvent.progress,
                    swipeEdge = edge,
                    touchX = backEvent.touchX,
                    touchY = backEvent.touchY
                )
            )
        }

        override fun handleOnBackPressed() { dispatcher.back() }

        @RequiresApi(34)
        override fun handleOnBackCancelled() { dispatcher.cancelPredictiveBack() }
    }

    this.onBackPressedDispatcher.addCallback(callback)

    dispatcher.addEnabledChangedListener { dispatcherIsEnabled ->
        callback.isEnabled = dispatcherIsEnabled
    }

    return dispatcher
}