package com.nxoim.decomposite.core.wasm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.nxoim.decomposite.core.common.navigation.CommonNavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.InternalNavigationRootApi
import com.nxoim.decomposite.core.common.navigation.NavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import kotlinx.browser.window

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

fun defaultNavigationRootData() = NavigationRootData()