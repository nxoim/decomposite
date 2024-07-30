package com.nxoim.decomposite.core.common.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.nxoim.decomposite.core.common.navigation.animations.materialContainerMorph
import com.nxoim.decomposite.core.common.ultils.LocalBackDispatcher
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import com.nxoim.decomposite.core.common.viewModel.LocalViewModelStore
import com.nxoim.decomposite.core.common.viewModel.ViewModelStore
import kotlin.math.roundToInt


/**
 * Creates the root of the app for back gesture handling, snack content controller,
 * storing view models, and navigation controller instances. View model store by default
 * is wrapped into default component context's instance keeper.
 *
 * Initialize this outside of setContent.
 */
@Immutable
data class NavigationRootData(
	val defaultComponentContext: DefaultComponentContext = DefaultComponentContext(
		LifecycleRegistry(),
		StateKeeperDispatcher(savedState = null)
	),
	val navStore: NavControllerStore = NavControllerStore(),
	val viewModelStore: ViewModelStore = defaultComponentContext.instanceKeeper.getOrCreateSimple {
		ViewModelStore()
	}
)

/**
 * Sets up the navigation root. Is used for managing overlays and snack content. Contains
 * information about the screen for some animations, like [materialContainerMorph].
 */
@Immutable
class NavigationRoot(val screenInformation: ScreenInformation) {
	internal val overlays = mutableStateListOf<@Composable () -> Unit>()

	@Composable
	internal fun overlay(content: @Composable () -> Unit) {
		remember { overlays.add(content) }
		DisposableEffect(Unit) { onDispose { overlays.remove(content) } }
	}
}

/**
 * Provides navigation controller and view model stores, default component context, navigation
 * root for overlays, and the back dispatcher vis [CompositionLocalProvider], displays overlays.
 */
@InternalNavigationRootApi
@NonRestartableComposable
@Composable
fun CommonNavigationRootProvider(
	navigationRoot: NavigationRoot,
	navigationRootData: NavigationRootData,
	content: @Composable () -> Unit
) = CompositionLocalProvider(
	LocalNavControllerStore provides navigationRootData.navStore,
	LocalViewModelStore provides navigationRootData.viewModelStore,
	LocalComponentContext provides navigationRootData.defaultComponentContext,
	LocalNavigationRoot provides navigationRoot,
	LocalBackDispatcher provides navigationRootData
		.defaultComponentContext
		.backHandler as BackDispatcher,
	content = {
		content()

		navigationRoot.overlays.forEach { it() }
	}
)

/**
 * Fallback implementation of a navigation root provider. Uses BoxWithConstraints to provide
 * the screen size, which is possibly incorrect on some platforms. Please use a platform
 * implementation. You are welcome to open an issue or PR.
 */
@FallbackNavigationRootImplementation
@NonRestartableComposable
@Composable
fun NavigationRootProvider(
	navigationRootData: NavigationRootData,
	content: @Composable () -> Unit
) {
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
			navigationRootData,
			content
		)
	}
}

/**
 * Provides some data from the root of the app for displaying overlays and other things.
 */
val LocalNavigationRoot = staticCompositionLocalOf<NavigationRoot> {
	error("No NavigationRoot provided")
}

@RequiresOptIn(
	message = """This is a fallback implementation. Using it might resolve in unintended behavior
        or some platform-dependent features may be lacking, which at the moment is only correct 
        screen size calculations. Please open an issue at https://github.com/nxoim/decomposite/issues
        if you would like to contribute.
    """
)
annotation class FallbackNavigationRootImplementation()

@RequiresOptIn
annotation class InternalNavigationRootApi()

