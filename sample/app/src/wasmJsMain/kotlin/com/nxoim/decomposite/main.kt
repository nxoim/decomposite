package com.nxoim.decomposite

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.nxoim.decomposite.core.common.navigation.BackGestureProviderContainer
import com.nxoim.decomposite.core.wasm.navigation.NavigationRootProvider
import com.nxoim.decomposite.core.wasm.navigation.defaultNavigationRootData
import com.nxoim.decomposite.ui.theme.SampleTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val navigationRootData = defaultNavigationRootData()

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        SampleTheme {
            @OptIn(ExperimentalDecomposeApi::class)
            BackGestureProviderContainer(navigationRootData.defaultComponentContext) {
                NavigationRootProvider(navigationRootData) {
                    App()
                }
            }
        }
    }
}