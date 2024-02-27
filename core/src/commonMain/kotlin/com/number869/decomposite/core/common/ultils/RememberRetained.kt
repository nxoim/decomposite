package com.number869.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import com.number869.decomposite.core.common.navigation.NavHost


/**
 * Boilerplate free alternative to [rememberSaveable]. Remembers and stores the value in
 * the [InstanceKeeper] tied to local [ComponentContext] provided by [LocalComponentContext],
 * meaning the value will be retained as long as the component/backstack entry (provided
 * by [NavHost]) exists
 */
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