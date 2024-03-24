package com.number869.decomposite.core.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.number869.decomposite.core.common.ultils.ContentType
import com.number869.decomposite.core.common.ultils.LocalComponentContext
import com.number869.decomposite.core.common.ultils.OnDestinationDisposeEffect
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

/**
 * Gets an existing navigation controller instance.
 */
@ReadOnlyComposable
@Composable
inline fun <reified T : Any> getExistingNavController(
    navStore: NavControllerStore = LocalNavControllerStore.current
) = navStore.get<T>()

@Composable
inline fun <reified C : Any> navController(
    startingDestination: C,
    serializer: KSerializer<C>? = null,
    navStore: NavControllerStore = LocalNavControllerStore.current,
    componentContext: ComponentContext = LocalComponentContext.current,
    noinline childFactory: (
        config: C,
        childComponentContext: ComponentContext
    ) -> DecomposeChildInstance = { _, childComponentContext ->
        DefaultChildInstance(childComponentContext)
    }
): NavController<C> {
    OnDestinationDisposeEffect(
        C::class.toString() + " NavHost OnDestinationDisposeEffect",
        waitForCompositionRemoval = true
    ) {
        navStore.remove<C>()
    }

    return remember(componentContext) {
        navStore.getOrCreate<C> {
            NavController(startingDestination, serializer ?: serializer(), componentContext, childFactory)
        }
    }
}

//@Composable
//inline fun <reified T : Any> navController(
//    navStore: NavControllerStore = LocalNavControllerStore.current
//) = remember { navStore.get<T>() }

inline fun <reified T : Any> getNavController(navStore: NavControllerStore) = navStore.get<T>()

@Immutable
class NavController<C : Any>(
    private val startingDestination: C,
    serializer: KSerializer<C>? = null,
    componentContext: ComponentContext,
    childFactory: (
        config: C,
        childComponentContext: ComponentContext
    ) -> DecomposeChildInstance = { _, childComponentContext ->
        DefaultChildInstance(childComponentContext)
    }
) : ComponentContext by componentContext {
    private val scope = MainScope()

    private val screenNavigation = StackNavigation<C>()
    private val overlayNavigation = StackNavigation<C>()

    val screenStack = childStack(
        source = screenNavigation,
        serializer = serializer,
        initialConfiguration = startingDestination,
        key = "screenStack" + startingDestination::class.toString(),
        handleBackButton = true,
        childFactory = childFactory
    )

    val overlayStack = childStack(
        source = overlayNavigation,
        serializer = serializer,
        initialConfiguration = startingDestination,
        key = "overlayStack" + startingDestination::class.toString(),
        handleBackButton = true,
        childFactory = childFactory
    )

    private val _currentScreen = MutableStateFlow(screenStack.active.configuration)
    val currentScreen: StateFlow<C> get() = _currentScreen

    init {
        scope.launch {
            screenStack.subscribe { _currentScreen.value = it.active.configuration }
        }
    }

    fun navigate(
        destination: C,
        type: ContentType = ContentType.Contained,
        useBringToFront: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        when (type) {
            ContentType.Contained -> {
                if (useBringToFront)
                    screenNavigation.bringToFront(destination)
                else
                    screenNavigation.navigate(
                        transformer = { stack -> stack.filterNot { it == destination } + destination },
                        onComplete = { _, _ -> onComplete() },
                    )

                overlayNavigation.replaceAll(startingDestination)
            }

            ContentType.Overlay -> {
                if (useBringToFront)
                    overlayNavigation.bringToFront(destination)
                else
                    overlayNavigation.navigate(
                        transformer = { stack -> stack.filterNot { it == destination } + destination },
                        onComplete = { _, _ -> onComplete() },
                    )
            }
        }
    }

    fun navigateBack(onComplete: (Boolean) -> Unit = { }) {
        if (overlayStack.active.configuration != startingDestination) {
            overlayNavigation.pop(onComplete)
        } else {
            screenNavigation.pop(onComplete)
        }
    }

    fun navigateBackTo(destination: C, type: ContentType, onComplete: (isSuccess: Boolean) -> Unit = { }) {
        when (type) {
            ContentType.Contained -> {
                val indexOfDestination = screenStack.backStack.indexOfFirst { it.configuration == destination }
                screenNavigation.popTo(indexOfDestination) { onComplete(it) }
            }

            ContentType.Overlay -> {
                val indexOfDestination = overlayStack.backStack.indexOfFirst { it.configuration == destination }
                overlayNavigation.popTo(indexOfDestination) { onComplete(it) }
            }
        }
    }

    fun close(destination: C, type: ContentType, onComplete: () -> Unit = { }) {
        when (type) {
            ContentType.Contained -> {
                screenNavigation.navigate(
                    transformer = { stack -> stack.filterNot { it == destination } },
                    onComplete = { _, _ -> onComplete() },
                )
            }

            ContentType.Overlay -> {
                overlayNavigation.navigate(
                    transformer = { stack -> stack.filterNot { it == destination } },
                    onComplete = { _, _ -> onComplete() },
                )
            }
        }
    }

    fun replaceCurrentScreen(destination: C, type: ContentType = ContentType.Contained, onComplete: () -> Unit = {}) {
        when (type) {
            ContentType.Contained -> screenNavigation.replaceCurrent(destination) { onComplete() }
            ContentType.Overlay -> overlayNavigation.replaceCurrent(destination) { onComplete() }
        }
    }
    fun replaceAll(destination: C, type: ContentType = ContentType.Contained, onComplete: () -> Unit = {}) {
        when (type) {
            ContentType.Contained -> screenNavigation.replaceAll(destination) { onComplete() }
            ContentType.Overlay -> overlayNavigation.replaceAll(destination) { onComplete() }
        }
    }
}

@JvmInline
@Immutable
value class DefaultChildInstance(override val componentContext: ComponentContext) : DecomposeChildInstance

interface DecomposeChildInstance {
    val componentContext: ComponentContext
}
