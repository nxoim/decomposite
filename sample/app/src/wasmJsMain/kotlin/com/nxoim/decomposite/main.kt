package com.nxoim.decomposite

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.nxoim.decomposite.core.common.navigation.FallbackNavigationRootImplementation
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.navigation.NavigationRootProvider

@OptIn(ExperimentalComposeUiApi::class, FallbackNavigationRootImplementation::class)
fun main() {
    val navigationRootData = NavigationRootData()

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        NavigationRootProvider(navigationRootData) {
            App()
        }
    }
}