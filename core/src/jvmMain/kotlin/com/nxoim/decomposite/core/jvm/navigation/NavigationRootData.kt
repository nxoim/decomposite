package com.nxoim.decomposite.core.jvm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.FrameWindowScope
import com.arkivanov.decompose.ComponentContext
import com.nxoim.decomposite.core.common.navigation.CommonNavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.InternalNavigationRootApi
import com.nxoim.decomposite.core.common.navigation.NavControllerStore
import com.nxoim.decomposite.core.common.navigation.NavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import com.nxoim.decomposite.core.common.viewModel.ViewModelStore

/**
 * JVM specific [NavigationRoot] and [NavigationRootData] provider.
 * Collects the max window size for animations.
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

    @OptIn(InternalNavigationRootApi::class)
    CommonNavigationRootProvider(
        remember { NavigationRoot(screenInformation) },
        navigationRootData,
        content
    )
}

/**
 * Creates an JVM specific root of the app for back gesture handling,
 * storing view models, and navigation controller instances.
 *
 * [NavigationRootData] is responsible for setting up the root context for navigation and
 * state management within the application. It provides access to essential
 * components like the [ViewModelStore], [NavControllerStore], and the default
 * [ComponentContext].
 *
 * [ViewModelStore] by default is wrapped into the default component context's instance keeper.
 *
 * Initialize this outside of Window.
 *
 * Example:
 * ```kotlin
 * fun main() = application {
 *     val navigationRootData = defaultNavigationRootData()
 *
 *     Window(
 *         title = "Example",
 *         onCloseRequest = ::exitApplication,
 *     ) {
 *         SampleTheme {
 *             Surface {
 *                 NavigationRootProvider(navigationRootData) { App() }
 *             }
 *         }
 *     }
 * }
 * ```
 */
fun defaultNavigationRootData() = NavigationRootData()