package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler

@Composable
fun BackGestureHandler(
    enabled: Boolean = true,
    backHandler: BackHandler = LocalComponentContext.current.backHandler,
    onBackStarted: (BackEvent) -> Unit = {},
    onBackProgressed: (BackEvent) -> Unit = {},
    onBackCancelled: () -> Unit = {},
    onBack: () -> Unit,
) {
    if (enabled) DisposableEffect(backHandler) {
        val callback = BackCallback(
            onBackStarted = onBackStarted,
            onBackProgressed = onBackProgressed,
            onBackCancelled = onBackCancelled,
            onBack = onBack,
        )

        backHandler.register(callback)
        onDispose { backHandler.unregister(callback) }
    }
}