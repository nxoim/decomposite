package com.number869.decomposite.core.common.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.number869.decomposite.core.common.navigation.animations.ContentAnimations
import com.number869.decomposite.core.common.navigation.animations.fade
import com.number869.decomposite.core.common.ultils.ContentType
import com.number869.decomposite.core.common.ultils.LocalComponentContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Gets an existing navigation controller instance.
 */
@ReadOnlyComposable
@Composable
inline fun <reified T : Any> navController(
    navStore: NavControllerStore = LocalNavControllerStore.current
) = navStore.get<T>()

@Composable
inline fun <reified C : Any> navController(
    startingDestination: C,
    serializer: KSerializer<C>? = null,
    navStore: NavControllerStore = LocalNavControllerStore.current,
    componentContext: ComponentContext = LocalComponentContext.current
) = remember {
    navStore.getOrCreate<C> {
        NavController(startingDestination, serializer ?: serializer(), componentContext)
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
    componentContext: ComponentContext
) : ComponentContext by componentContext {
    private val scope = MainScope()
    private val mutex = Mutex()

    val contentOfSnacks = instanceKeeper.getOrCreate("contentOfSnacks ${startingDestination::class}") {
        SnacksContentHolder(mutableStateMapOf<String, @Composable () -> Unit>())
    }.data

    val animationsForDestinations = instanceKeeper.getOrCreate("animationsForDestinations ${startingDestination::class}") {
        AnimationsHolder(mutableMapOf<String, ContentAnimations>())
    }.data

    private val screenNavigation = StackNavigation<C>()
    private val overlayNavigation = StackNavigation<C>()
    private val snackNavigation = StackNavigation<String>()

    val screenStack = childStack(
        source = screenNavigation,
        serializer = serializer,
        initialConfiguration = startingDestination,
        key = "screenStack" + startingDestination::class.toString(),
        handleBackButton = true,
        childFactory = { config, componentContext -> DecomposeChildInstance(config, componentContext) }
    )

    val overlayStack = childStack(
        source = overlayNavigation,
        serializer = serializer,
        initialConfiguration = startingDestination,
        key = "overlayStack" + startingDestination::class.toString(),
        handleBackButton = true,
        childFactory = { config, context -> DecomposeChildInstance(config, context) ; }
    )

    val snackStack = childStack(
        source = snackNavigation,
        serializer = String.serializer(),
        initialConfiguration = "empty",
        key = "snackStack" + startingDestination::class.toString(),
        handleBackButton = false,
        childFactory = { config, context -> DecomposeChildInstance(config, context) }
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
                val stackWithoutThisKeyAsArrayOfKeys = screenStack.backStack
                    .filterNot { it.configuration == destination }
                    .map { it.configuration as Any }
                    .toTypedArray()

                screenNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys as Array<C>) {
                    onComplete()
                }
            }

            ContentType.Overlay -> {
                val stackWithoutThisKeyAsArrayOfKeys = overlayStack.backStack
                    .filterNot { it.configuration == destination }
                    .map { it.configuration as Any }
                    .toTypedArray()

                overlayNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys as Array<C>) {
                    onComplete()
                }
            }
        }
    }

    fun close(destination: Child.Created<C, DecomposeChildInstance<C>>, type: ContentType, onComplete: () -> Unit = { }) {
        when (type) {
            ContentType.Contained -> {
                val stackWithoutThisKeyAsArrayOfKeys = screenStack.backStack
                    .filterNot { it == destination }
                    .map { it.configuration as Any }
                    .toTypedArray()

                screenNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys as Array<C>) {
                    onComplete()
                }
            }

            ContentType.Overlay -> {
                val stackWithoutThisKeyAsArrayOfKeys = overlayStack.backStack
                    .filterNot { it.configuration == destination }
                    .map { it.configuration as Any }
                    .toTypedArray()

                overlayNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys as Array<C>) {
                    onComplete()
                }
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

    fun openInSnack(
        key: String,
        animation: () -> ContentAnimations = { fade() },
        displayDurationMillis: Duration = 5.seconds,
        content: @Composable BoxScope.() -> Unit
    ) {
        scope.launch(Dispatchers.Main) {
            mutex.withLock {
                // remember data about content. the content is removed from within
                // the nav host using DisposableEffect for proper animations using
                animationsForDestinations[key] = animation()
                contentOfSnacks[key] = { Box(content = content, modifier = Modifier.fillMaxSize()) }

                snackNavigation.push(key)
                delay(displayDurationMillis)
                closeSnack(key)

                // delay before displaying another snack
                delay(150)
            }
        }
    }

    fun removeSnackContents(key: String) {
        contentOfSnacks.remove(key)
        animationsForDestinations.remove(key)
    }

    fun closeSnack(key: String, onComplete: () -> Unit = { }) {
        val stackWithoutThisKeyAsArrayOfKeys = snackStack.backStack
            .filterNot { it.configuration == key }
            .map { it.configuration }
            .toTypedArray()

        snackNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys) { onComplete() }
    }
}

@Immutable
class DecomposeChildInstance<C>(val config: C, val componentContext: ComponentContext)

@Immutable
private data class AnimationsHolder<T>(val data: T) : InstanceKeeper.Instance
@Immutable
private data class SnacksContentHolder<T>(val data: T) : InstanceKeeper.Instance
