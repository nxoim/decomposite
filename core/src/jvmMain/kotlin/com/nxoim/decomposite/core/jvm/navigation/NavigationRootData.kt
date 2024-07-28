package com.nxoim.decomposite.core.jvm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.FrameWindowScope
import com.nxoim.decomposite.core.common.navigation.CommonNavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.NavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape

/**
 * JVM specific navigation root provider. Collects the max window size for animations.
 * Uses [CommonNavigationRootProvider].
 */
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
        remember { NavigationRoot(screenInformation) },
        navigationRootData,
        content
    )
}

/**
 * Creates a default platform-specific instance of [NavigationRootData].
 *
 * Initialize this outside of setContent and provide to [NavigationRootProvider].
 */
fun defaultNavigationRootData() = NavigationRootData()