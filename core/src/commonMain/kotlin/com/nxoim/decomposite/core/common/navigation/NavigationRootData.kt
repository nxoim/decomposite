package com.nxoim.decomposite.core.common.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.nxoim.decomposite.core.common.navigation.snacks.SnackController
import com.nxoim.decomposite.core.common.navigation.snacks.SnackHost
import com.nxoim.decomposite.core.common.ultils.LocalBackDispatcher
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import com.nxoim.decomposite.core.common.viewModel.LocalViewModelStore
import com.nxoim.decomposite.core.common.viewModel.ViewModelStore
import kotlin.math.roundToInt

/**
 * Initialize this outside of setContent in the activity and then just wrap the root of your
 * compose content ON YOUR PLATFORM (unless you provide default component context via
 * dependency injection) with [LocalNavControllerStore] so that the nav hosts can access it.
 */
@Immutable
data class NavigationRootData(
    val defaultComponentContext: ComponentContext = DefaultComponentContext(
        LifecycleRegistry(),
        StateKeeperDispatcher(savedState = null)
    ),
    val navStore: NavControllerStore = NavControllerStore(),
    val viewModelStore: ViewModelStore = defaultComponentContext.instanceKeeper.getOrCreateSimple {
        ViewModelStore()
    },
    val snackController: SnackController = SnackController(defaultComponentContext)
)

@Immutable
class NavigationRoot(
    val snackController: SnackController,
    val screenInformation: ScreenInformation
) {
    val overlays = mutableStateListOf<@Composable () -> Unit>()

    @Composable
    fun overlay(content: @Composable () -> Unit) {
        remember { overlays.add(content) }
        DisposableEffect(Unit) { onDispose { overlays.remove(content) } }
    }
}

@NonRestartableComposable
@Composable
internal fun CommonNavigationRootProvider(
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

        SnackHost()
    }
)

@FallbackNavigationRootImplementation
@NonRestartableComposable
@Composable
fun NavigationRootProvider(navigationRootData: NavigationRootData, content: @Composable () -> Unit) {
    BoxWithConstraints {
        val screenInformation = ScreenInformation(
            widthPx = (this.maxWidth * LocalDensity.current.density).value.roundToInt(),
            heightPx = (this.maxHeight * LocalDensity.current.density).value.roundToInt(),
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
}

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

