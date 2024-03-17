package com.number869.decomposite.core.common.ultils

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import com.number869.decomposite.core.common.navigation.NavHost
import kotlin.random.Random


/**
 * Boilerplate free alternative to [rememberSaveable]. Remembers and stores the value in
 * the [InstanceKeeper] tied to local [ComponentContext] provided by [LocalComponentContext],
 * meaning the value will be retained as long as the component/backstack entry (provided
 * by [NavHost]) exists
 */
@OptIn(InternalDecomposeApi::class)
@Stable
@Composable
inline fun <reified T : Any> rememberRetained(
    key: String = rememberSaveable { Random(99).toString() } + T::class.hashString(),
    componentContext: ComponentContext = LocalComponentContext.current,
    crossinline block: @DisallowComposableCalls () -> T
): T {
    var previousKey by rememberSaveable() { mutableStateOf(key) }

    return remember(key) {
        if (key != previousKey) {
            componentContext.instanceKeeper.remove(previousKey)
            previousKey = key.hashString()
        }

        componentContext.instanceKeeper.getOrCreateSimple(previousKey, block)
    }
}
