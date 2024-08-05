package com.nxoim.decomposite

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.CanvasBasedWindow
import com.nxoim.decomposite.core.common.navigation.CommonNavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.InternalNavigationRootApi
import com.nxoim.decomposite.core.common.navigation.NavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
	val navigationRootData = NavigationRootData()

	CanvasBasedWindow(canvasElementId = "ComposeTarget") {
		BoxWithConstraints {
			val screenInformation = ScreenInformation(
				widthPx = (this.maxWidth * LocalDensity.current.density).value.roundToInt(),
				heightPx = (this.maxHeight * LocalDensity.current.density).value.roundToInt(),
				screenShape = ScreenShape(
					path = null,
					corners = null
				)
			)

			@OptIn(InternalNavigationRootApi::class)
			CommonNavigationRootProvider(
				remember { NavigationRoot(screenInformation) },
				navigationRootData
			) {
                App()
			}
		}
	}
}