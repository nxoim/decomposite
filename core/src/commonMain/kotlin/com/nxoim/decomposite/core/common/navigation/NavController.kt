package com.nxoim.decomposite.core.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.nxoim.decomposite.core.common.ultils.ContentType
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import com.nxoim.decomposite.core.common.ultils.OnDestinationDisposeEffect
import com.nxoim.decomposite.core.common.ultils.activeAsState
import com.nxoim.decomposite.core.common.ultils.rememberRetained
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

/**
 * Gets an existing navigation controller instance
 */
@ReadOnlyComposable
@Composable
inline fun <reified C : Any> getExistingNavController(
	key: String? = null,
	navStore: NavControllerStore = LocalNavControllerStore.current
) = navStore.get(key, C::class)

/**
 * Creates a navigation controller instance in the [NavControllerStore], which allows
 * for sharing the same instance between multiple calls of [navController] or [getExistingNavController].
 *
 * Is basically a decompose component that replicates the functionality of a generic
 * navigation controller. The instance is not retained, therefore on configuration changes
 * components will die and get recreated. By default inherits parent's [ComponentContext].
 *
 * [childFactory] allows for creating custom children instances that implement [DecomposeChildInstance].
 *
 * [key] is used for identifying [childStack]'s during serialization and instances in
 * [NavControllerStore], which means keys MUST be unique.
 *
 * On death removes itself from the [NavControllerStore] right after the composition's death.
 */
@Composable
inline fun <reified C : Any> navController(
	startingDestination: C,
	serializer: KSerializer<C>? = serializer(),
	navStore: NavControllerStore = LocalNavControllerStore.current,
	componentContext: ComponentContext = LocalComponentContext.current,
	key: String? = null,
	noinline childFactory: (
		config: C,
		childComponentContext: ComponentContext
	) -> DecomposeChildInstance = { _, childComponentContext ->
		DefaultChildInstance(childComponentContext)
	}
): NavController<C> {
	val kClass = remember { C::class }

	OnDestinationDisposeEffect(
		"$kClass key $key navController OnDestinationDisposeEffect",
		waitForCompositionRemoval = true
	) {
		navStore.remove(key, kClass)
	}

	return remember(componentContext) {
		navStore.getOrCreate(key, kClass) {
			NavController(
				startingDestination,
				serializer,
				componentContext,
				key ?: kClass.toString(),
				childFactory
			)
		}
	}
}

inline fun <reified C : Any> getNavController(
	key: String? = null,
	navStore: NavControllerStore
) = navStore.get(key, C::class)

/**
 * Generic navigation controller. Contains a stack for overlays and a stack for screens.
 */
@Immutable
class NavController<C : Any>(
	private val startingDestination: C,
	serializer: KSerializer<C>? = null,
	componentContext: ComponentContext,
	key: String = startingDestination::class.toString(),
	childFactory: (
		config: C,
		childComponentContext: ComponentContext
	) -> DecomposeChildInstance = { _, childComponentContext ->
		DefaultChildInstance(childComponentContext)
	}
) : ComponentContext by componentContext {
	val screenNavigationController = StackNavigation<C>()
	val overlayNavigationController = StackNavigation<C>()

	val screenStack = childStack(
		source = screenNavigationController,
		serializer = serializer,
		initialConfiguration = startingDestination,
		key = "screenStack $key",
		handleBackButton = true,
		childFactory = childFactory
	)

	val overlayStack = childStack(
		source = overlayNavigationController,
		serializer = serializer,
		initialConfiguration = startingDestination,
		key = "overlayStack $key",
		handleBackButton = true,
		childFactory = childFactory
	)

	val currentScreen by screenStack.activeAsState()

	/**
	 * Navigates to a destination. If a destination exists already - moves it to the top instead
	 * of adding a new entry. If the [removeIfIsPreceding] is enabled (is by default) and
	 * the requested [destination] precedes the current one in the stack -
	 * navigate back instead.
	 */
	fun navigate(
		destination: C,
		type: ContentType = ContentType.Contained,
		// removes the current entry if requested navigation to the preceding one
		removeIfIsPreceding: Boolean = true,
		onComplete: () -> Unit = {}
	) = when (type) {
		ContentType.Contained -> {
			screenNavigationController.navigate(
				transformer = { stack ->
					if (removeIfIsPreceding && stack.size > 1 && stack[stack.lastIndex - 1] == destination)
						stack.dropLast(1)
					else
						stack.filterNot { it == destination } + destination
				},
				onComplete = { _, _ -> onComplete() }
			)

			overlayNavigationController.replaceAll(startingDestination)
		}

		ContentType.Overlay -> {
			overlayNavigationController.navigate(
				transformer = { stack ->
					if (removeIfIsPreceding && stack.size > 1 && stack[stack.lastIndex - 1] == destination)
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
			overlayNavigationController.pop(onComplete)
		} else {
			screenNavigationController.pop(onComplete)
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
			val indexOfDestination =
				screenStack.backStack.indexOfFirst { it.configuration == destination }
			screenNavigationController.popTo(indexOfDestination, onComplete)
		}

		ContentType.Overlay -> {
			val indexOfDestination =
				overlayStack.backStack.indexOfFirst { it.configuration == destination }
			overlayNavigationController.popTo(indexOfDestination, onComplete)
		}
	}

	/**
	 * Removes a destination.
	 */
	fun close(destination: C, type: ContentType, onComplete: () -> Unit = { }) = when (type) {
		ContentType.Contained -> {
			screenNavigationController.navigate(
				transformer = { stack -> stack.filterNot { it == destination } },
				onComplete = { _, _ -> onComplete() }
			)
		}

		ContentType.Overlay -> {
			overlayNavigationController.navigate(
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
		ContentType.Contained -> screenNavigationController.replaceCurrent(withDestination) { onComplete() }
		ContentType.Overlay -> overlayNavigationController.replaceCurrent(withDestination) { onComplete() }
	}

	/**
	 * Replaces all destinations with the provided one.
	 */
	fun replaceAll(
		vararg destination: C,
		type: ContentType = ContentType.Contained,
		onComplete: () -> Unit = {}
	) = when (type) {
		ContentType.Contained -> screenNavigationController.replaceAll(*destination) { onComplete() }
		ContentType.Overlay -> overlayNavigationController.replaceAll(*destination) { onComplete() }
	}
}

@JvmInline
@Immutable
value class DefaultChildInstance(
	override val componentContext: ComponentContext
) : DecomposeChildInstance

/**
 * Base for child instances. Contains [componentContext] for features like [rememberRetained],
 * [OnDestinationDisposeEffect], [LocalComponentContext].
 */
interface DecomposeChildInstance {
	val componentContext: ComponentContext
}
