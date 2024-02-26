package com.number869.decomposite.core.common.navigation

import androidx.compose.runtime.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.number869.decomposite.core.common.ultils.LocalComponentContext
import com.number869.decomposite.core.common.viewModel.LocalViewModelStore
import com.number869.decomposite.core.common.viewModel.ViewModelStore

@Immutable
class NavigationRootData(
    val defaultComponentContext: DefaultComponentContext,
    val navStore: NavControllerStore,
    val viewModelStore: ViewModelStore
)

/**
 * Initialize this outside of setContent in the activity and then just wrap the root of your
 * compose content ON YOUR PLATFORM (unless you provide default component context via
 * dependency injection) with [LocalNavControllerStore] so that the nav hosts can access it.
 */
fun navigationRootDataProvider(
    componentContext: DefaultComponentContext? = null,
    navStore: NavControllerStore = NavControllerStore(),
    viewModelStore: ViewModelStore = ViewModelStore()
): NavigationRootData {
    val anyComponentContext = componentContext ?: DefaultComponentContext(
        LifecycleRegistry(),
        StateKeeperDispatcher(savedState = null)
    )

    return NavigationRootData(
        defaultComponentContext = anyComponentContext,
        navStore = navStore,
        viewModelStore = anyComponentContext.instanceKeeper.getOrCreateSimple { viewModelStore }
    )
}

@Immutable
class NavigationRoot {
    val overlays = mutableStateListOf<@Composable () -> Unit>()
    @Composable
    fun overlay(content: @Composable () -> Unit) {
        overlays.add(content)

        DisposableEffect(Unit) { onDispose { overlays.remove(content) } }
    }
}

val LocalNavigationRoot = staticCompositionLocalOf<NavigationRoot> {
    error("No NavigationRoot provided")
}

@Composable
fun NavigationRoot(navigationRootData: NavigationRootData, content: @Composable () -> Unit) {
    with(remember { NavigationRoot() }) {
        CompositionLocalProvider(
            LocalNavControllerStore provides navigationRootData.navStore,
            LocalViewModelStore provides navigationRootData.viewModelStore,
            LocalComponentContext provides navigationRootData.defaultComponentContext,
            LocalNavigationRoot provides this,
            content = {
                content()

                overlays.forEach { it() }
            }
        )
    }
}

