package com.nxoim.decomposite.core.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.FrameWindowScope
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape

@NonRestartableComposable
@Composable
fun FrameWindowScope.NavigationRootProvider(navigationRootData: NavigationRootData, content: @Composable () -> Unit) {
    val screenInformation = ScreenInformation(
        widthPx = window.maximumSize.width,
        heightPx = window.maximumSize.height,
        screenShape = ScreenShape(
            path = null,
            corners = null
        )
    )

    CommonNavigationRootProvider(
        remember { NavigationRoot(navigationRootData.snackController, screenInformation) },
        navigationRootData,
        content
    )
}