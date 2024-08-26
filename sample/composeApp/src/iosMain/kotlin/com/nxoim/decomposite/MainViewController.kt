package com.nxoim.decomposite

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.nxoim.decomposite.core.common.navigation.BackGestureProviderContainer
import com.nxoim.decomposite.core.common.navigation.FallbackNavigationRootImplementation
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.ios.navigation.NavigationRootProvider
import com.nxoim.decomposite.ui.theme.SampleTheme
import platform.CoreGraphics.CGFloat

@OptIn(ExperimentalDecomposeApi::class)
fun MainViewController(
    navigationRootData: NavigationRootData,
    screenWidth: CGFloat,
    screenHeight: CGFloat,
) = ComposeUIViewController {
    BackGestureProviderContainer(navigationRootData.defaultComponentContext) {
        SampleTheme {
            NavigationRootProvider(
                navigationRootData,
                screenWidth,
                screenHeight
            ) {
                App()
            }
        }
    }
}