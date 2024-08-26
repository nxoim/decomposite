package com.nxoim.decomposite.core.common.ultils

import com.arkivanov.essenty.backhandler.BackEvent

sealed interface BackGestureEvent {
    data object None : BackGestureEvent
    data class OnBackStarted(val event: BackEvent) : BackGestureEvent
    data class OnBackProgressed(val event: BackEvent) : BackGestureEvent
    data object OnBackCancelled : BackGestureEvent
    data object OnBack : BackGestureEvent
}