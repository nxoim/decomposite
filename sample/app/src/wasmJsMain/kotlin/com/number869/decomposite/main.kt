package com.number869.decomposite

import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.number869.decomposite.core.common.navigation.navigationRootDataProvider
import org.koin.core.context.startKoin
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin { modules(module { navigationRootDataProvider() }) }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        runCatching {
            App()
        }.onFailure {
            Text(it.message ?: "Unknown error")
        }
    }
}