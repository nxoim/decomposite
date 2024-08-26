package com.nxoim.decomposite.core.wasm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.nxoim.decomposite.core.common.navigation.CommonNavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.InternalNavigationRootApi
import com.nxoim.decomposite.core.common.navigation.NavControllerStore
import com.nxoim.decomposite.core.common.navigation.NavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import com.nxoim.decomposite.core.common.viewModel.ViewModelStore
import kotlinx.browser.window

/**
 * WASMJS specific [NavigationRoot] and [NavigationRootData] provider.
 * Collects the screen size and shape for animations.
 */
@Composable
fun NavigationRootProvider(
	navigationRootData: NavigationRootData,
	content: @Composable () -> Unit
) {
	val screenInformation = ScreenInformation(
		widthPx = window.screen.width,
		heightPx = window.screen.height,
		screenShape = ScreenShape(path = null, corners = null)
	)

	@OptIn(InternalNavigationRootApi::class)
	CommonNavigationRootProvider(
		remember(screenInformation) { NavigationRoot(screenInformation) },
		navigationRootData,
		content
	)
}

/**
 * Creates an WASMJS specific root of the app for back gesture handling,
 * storing view models, and navigation controller instances.
 *
 * [NavigationRootData] is responsible for setting up the root context for navigation and
 * state management within the application. It provides access to essential
 * components like the [ViewModelStore], [NavControllerStore], and the default
 * [ComponentContext].
 *
 * [ViewModelStore] by default is wrapped into the default component context's instance keeper.
 *
 * Initialize this outside of CanvasBasedWindow.
 *
 * Example:
 * ```kotlin
 * @OptIn(ExperimentalComposeUiApi::class)
 * fun main() {
 *     val navigationRootData = defaultNavigationRootData()
 *
 *     CanvasBasedWindow(canvasElementId = "ComposeTarget") {
 *         SampleTheme {
 *             NavigationRootProvider(navigationRootData) { App() }
 *         }
 *     }
 * }
 * ```
 */
fun defaultNavigationRootData() = NavigationRootData()