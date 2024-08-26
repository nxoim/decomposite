package com.nxoim.decomposite

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.nxoim.decomposite.core.common.navigation.BackGestureProviderContainer
import com.nxoim.decomposite.core.common.navigation.FallbackNavigationRootImplementation
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.ios.navigation.NavigationRootProvider
import com.nxoim.decomposite.ui.theme.SampleTheme
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.UIKit.UIScreen

@OptIn(ExperimentalDecomposeApi::class, ExperimentalForeignApi::class)
fun MainViewController(
    navigationRootData: NavigationRootData
) = ComposeUIViewController {
    BackGestureProviderContainer(navigationRootData.defaultComponentContext) {
        SampleTheme {
            NavigationRootProvider(navigationRootData) {
                App()
            }
        }
    }
}