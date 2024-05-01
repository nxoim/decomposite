package com.nxoim.decomposite.core.common.navigation

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.nxoim.decomposite.core.common.ultils.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

/**
 * Gets an existing navigation controller instance
 */
@ReadOnlyComposable
@Composable
inline fun <reified T : Any> getExistingNavController(
    navStore: NavControllerStore = LocalNavControllerStore.current
) = navStore.get<T>()

/**
 * Creates a navigation controller instance in the [NavControllerStore], which allows
 * for sharing the same instance between multiple calls of [navController] or [getExistingNavController].
 * Is basically a decompose component that replicates the functionality of a generic
 * navigation controller. The instance is not retained, therefore on configuration changes
 * components will die and get recreated. By default inherits parent's [ComponentContext].
 * [childFactory] allows for creating custom children instances that implement [DecomposeChildInstance].
 *
 * On death removes itself from the [NavControllerStore] right after the composition's death.
 */
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
            NavController(
                startingDestination,
                serializer ?: serializer(),
                componentContext,
                childFactory
            )
        }
    }
}

inline fun <reified T : Any> getNavController(navStore: NavControllerStore) = navStore.get<T>()

/**
 * Generic navigation controller. Contains a stack for overlays and a stack for screens.
 */
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

    val currentScreen by screenStack.activeAsState()

    /**
     * Navigates to a destination. If a destination exists already - moves it to the top instead
     * of adding a new entry. If the requested [destination] precedes the current one in the stack -
     * navigate back instead.
     */
    fun navigate(
        destination: C,
        type: ContentType = ContentType.Contained,
        useBringToFront: Boolean = false,
        onComplete: () -> Unit = {}
    ) = when (type) {
        ContentType.Contained -> {
            if (useBringToFront)
                screenNavigation.bringToFront(destination)
            else
                screenNavigation.navigate(
                    transformer = { stack ->
                        if (stack.size > 1 && stack[stack.lastIndex - 1] == destination)
                            stack.dropLast(1)
                        else
                            stack.filterNot { it == destination } + destination
                    },
                    onComplete = { _, _ -> onComplete() }
                )

            overlayNavigation.replaceAll(startingDestination)
        }

        ContentType.Overlay -> {
            if (useBringToFront)
                overlayNavigation.bringToFront(destination)
            else
                overlayNavigation.navigate(
                    transformer = { stack ->
                        if (stack.size > 1 && stack[stack.lastIndex - 1] == destination)
                            stack.dropLast(1)
                        else
                            stack.filterNot { it == destination } + destination
                    },
                    onComplete = { _, _ -> onComplete() }
                )
        }
    }

    /**
     * Navigates back in this(!) nav controller.
     */
    fun navigateBack(onComplete: (Boolean) -> Unit = { }) {
        if (overlayStack.active.configuration != startingDestination) {
            overlayNavigation.pop(onComplete)
        } else {
            screenNavigation.pop(onComplete)
        }
    }

    /**
     * Removes destinations that, in the stack, are after the provided one.
     */
    fun navigateBackTo(
        destination: C,
        type: ContentType,
        onComplete: (Boolean) -> Unit = { }
    ) = when (type) {
        ContentType.Contained -> {
            val indexOfDestination = screenStack.backStack.indexOfFirst { it.configuration == destination }
            screenNavigation.popTo(indexOfDestination, onComplete)
        }

        ContentType.Overlay -> {
            val indexOfDestination = overlayStack.backStack.indexOfFirst { it.configuration == destination }
            overlayNavigation.popTo(indexOfDestination, onComplete)
        }
    }

    /**
     * Gets rid of/closes a destination.
     */
    fun close(destination: C, type: ContentType, onComplete: () -> Unit = { }) = when (type) {
        ContentType.Contained -> {
            screenNavigation.navigate(
                transformer = { stack -> stack.filterNot { it == destination } },
                onComplete = { _, _ -> onComplete() }
            )
        }

        ContentType.Overlay -> {
            overlayNavigation.navigate(
                transformer = { stack -> stack.filterNot { it == destination } },
                onComplete = { _, _ -> onComplete() }
            )
        }
    }

    /**
     * Replaces the current destination with the provided one.
     */
    fun replaceCurrent(
        withDestination: C,
        type: ContentType,
        onComplete: () -> Unit = {}
    ) = when (type) {
        ContentType.Contained -> screenNavigation.replaceCurrent(withDestination) { onComplete() }
        ContentType.Overlay -> overlayNavigation.replaceCurrent(withDestination) { onComplete() }
    }

    /**
     * Replaces all destinations with the provided one.
     */
    fun replaceAll(
        vararg destination: C,
        type: ContentType = ContentType.Contained,
        onComplete: () -> Unit = {}
    ) = when (type) {
        ContentType.Contained -> screenNavigation.replaceAll(*destination) { onComplete() }
        ContentType.Overlay -> overlayNavigation.replaceAll(*destination) { onComplete() }
    }
}

@JvmInline
@Immutable
value class DefaultChildInstance(override val componentContext: ComponentContext) : DecomposeChildInstance

/**
 * Base for child instances. Contains [componentContext] for features like [rememberRetained],
 * [OnDestinationDisposeEffect], [LocalComponentContext].
 */
interface DecomposeChildInstance {
    val componentContext: ComponentContext
}
