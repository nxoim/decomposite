package com.number869.decomposite.core.common.ultils

import androidx.compose.runtime.Immutable
import com.arkivanov.essenty.backhandler.BackEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Immutable
class SharedBackEventScope() {
    // doubt the flow is the only way
    private val _gestureActions = MutableSharedFlow<BackGestureEvent>(extraBufferCapacity = 10)
    val gestureActions = _gestureActions.asSharedFlow()

    fun onBackStarted(backEvent: BackEvent) {
        _gestureActions.tryEmit(BackGestureEvent.OnBackStarted(backEvent))
    }

    fun onBackProgressed(backEvent: BackEvent) {
        _gestureActions.tryEmit(BackGestureEvent.OnBackProgressed(backEvent))
    }

    fun onBackCancelled() { _gestureActions.tryEmit(BackGestureEvent.OnBackCancelled) }

    fun onBack() { _gestureActions.tryEmit(BackGestureEvent.OnBack) }
}

sealed interface BackGestureEvent {
    data object None : BackGestureEvent
    data class OnBackStarted(val event: BackEvent) : BackGestureEvent
    data class OnBackProgressed(val event: BackEvent) : BackGestureEvent
    data object OnBackCancelled : BackGestureEvent
    data object OnBack : BackGestureEvent
}