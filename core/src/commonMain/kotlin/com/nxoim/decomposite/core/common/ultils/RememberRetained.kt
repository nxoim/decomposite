package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import com.nxoim.decomposite.core.common.navigation.NavHost
import kotlin.random.Random


/**
 * Boilerplate free alternative to [rememberSaveable]. Remembers and stores the value in
 * the [InstanceKeeper] tied to local [ComponentContext] provided by [LocalComponentContext],
 * meaning the value will be retained as long as the component/backstack entry (provided
 * by [NavHost]) exists
 */
@Stable
@Composable
inline fun <reified T : Any> rememberRetained(
    key: String = rememberSaveable { Random.nextFloat().toString() },
    componentContext: ComponentContext = LocalComponentContext.current,
    crossinline block: @DisallowComposableCalls () -> T
): T {
    var previousKey by rememberSaveable() { mutableStateOf(key) }

    return remember(key) {
        if (key != previousKey) {
            componentContext.instanceKeeper.remove(previousKey)
            previousKey = key
        }

        componentContext.instanceKeeper.getOrCreateSimple(previousKey, block)
    }
}
