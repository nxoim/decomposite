package com.number869.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple

@Composable
inline fun <reified T> rememberRetained(
    vararg key: Any,
    crossinline block: @DisallowComposableCalls () -> T
) : T {
    val localComponentContext = LocalComponentContext.current
    return remember {
        if (key.isEmpty())
            localComponentContext.instanceKeeper.getOrCreateSimple { block() }
        else
            localComponentContext.instanceKeeper.getOrCreateSimple(key) { block() }
    }
}