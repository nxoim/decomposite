package com.nxoim.decomposite.core.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import com.nxoim.decomposite.core.common.ultils.OnDestinationDisposeEffect
import com.nxoim.decomposite.core.common.ultils.rememberRetained
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

/**
 * Creates a navigation controller instance in the [NavControllerStore], which allows
 * for sharing the same instance between multiple calls of [navController].
 *
 * Is basically a decompose component that replicates the functionality of a generic
 * navigation controller. The instance is retained by default.
 *
 * @param [childFactory] allows for creating custom children instances that implement [DecomposeChildInstance].
 * @param [key] is used for identifying [childStack]'s during serialization and instances in
 * [NavControllerStore], which means keys MUST be unique.
 * @param [startingDestination] is the first destination in the stack.
 * @param [serializer] is used for preserving the state in case of process death.
 */
@Composable
inline fun <reified C : Any> navController(
	startingDestination: C,
	serializer: KSerializer<C>? = serializer(),
	navStore: NavControllerStore = LocalNavControllerStore.current,
	componentContext: ComponentContext = LocalComponentContext.current,
	key: String = navControllerKey<C>(),
	noinline childFactory: (
		config: C,
		childComponentContext: ComponentContext
	) -> DecomposeChildInstance = { _, childComponentContext ->
		DefaultChildInstance(childComponentContext)
	}
) = remember(componentContext, key) {
	// if the existing instance was already destroyed by the time
	// remember executes - delete it and make a new one
	navStore
		.get(key, C::class)
		?.parentComponentContext
		?.lifecycle
		?.state
		?.let { if (it == Lifecycle.State.DESTROYED) navStore.remove(key, C::class) }

	navStore.getOrCreate(key, C::class) {
		NavController(
			startingDestination,
			serializer,
			componentContext,
			key,
			childFactory
		)
	}
}
//	.also {
//		// TODO is this reliable
//	DisposableEffect(it) {
//		onDispose {
//			val lifecycleState = it.parentComponentContext.lifecycle.state
//			if (lifecycleState == Lifecycle.State.DESTROYED) navStore.remove(key, C::class)
//		}
//	}
//}


// During navigation a component context might get recreated (specifically
// when coming back to a component that's currently being removed from the
// stack animator), and if we do not create a new key for the new component context -
// navigation stops working in the component
inline fun <reified C : Any> navControllerKey(
	additionalKey: Any = ""
) = "${C::class}$additionalKey"

/**
 * Is basically a decompose component that replicates the functionality of a generic
 * navigation controller.
 *
 * @param [destinationFactory] allows for creating custom children instances that implement [DecomposeChildInstance].
 * @param [key] is used for identifying [childStack]'s during serialization and instances in
 * [NavControllerStore], which means keys MUST be unique.
 * @param [startingDestination] is the first destination in the stack.
 * @param [serializer] is used for preserving the state in case of process death.
 */
@Immutable
class NavController<C : Any>(
	startingDestination: C,
	serializer: KSerializer<C>? = null,
	val parentComponentContext: ComponentContext,
	val key: String,
	destinationFactory: (
		config: C,
		destinationComponentContext: ComponentContext
	) -> DecomposeChildInstance = { _, destinationComponentContext ->
		DefaultChildInstance(destinationComponentContext)
	}
) {
	val controller = StackNavigation<C>()

	val destinationStack = parentComponentContext.childStack(
		source = controller,
		serializer = serializer,
		initialConfiguration = startingDestination,
		key = "destinationStack $key",
		handleBackButton = true,
		childFactory = destinationFactory
	)

	val currentDestination by destinationStack.let {
		val state = mutableStateOf(it.value.active.configuration)

		it.subscribe { newState -> state.value = newState.active.configuration }

		return@let state
	}

	/**
	 * Navigates to a destination. If a destination exists already - moves it to the top instead
	 * of adding a new entry. If the [removeIfIsPreceding] is enabled (is by default) and
	 * the requested [destination] precedes the current one in the stack -
	 * navigate back instead.
	 */
	fun navigate(
		destination: C,
		// removes the current entry if requested navigation to the preceding one
		removeIfIsPreceding: Boolean = true,
		onComplete: () -> Unit = { }
	) {
		controller.navigate(
			transformer = { stack ->
				if (removeIfIsPreceding && stack.size > 1 && stack[stack.lastIndex - 1] == destination)
					stack.dropLast(1)
				else
					stack.filterNot { it == destination } + destination
			},
			onComplete = { _, _ -> onComplete() }
		)
	}

	/**
	 * Navigates back in this(!) nav controller.
	 */
	fun navigateBack(onComplete: (Boolean) -> Unit = { }) {
		controller.pop(onComplete)
	}

	/**
	 * Removes destinations that, in the stack, are after the provided one.
	 */
	fun navigateBackTo(
		destination: C,
		onComplete: (Boolean) -> Unit = { }
	) {
		val indexOfDestination = destinationStack.backStack
			.indexOfFirst { it.configuration == destination }

		controller.popTo(indexOfDestination, onComplete)
	}

	/**
	 * Removes a destination.
	 */
	fun close(destination: C, onComplete: () -> Unit = { }) = controller
		.navigate(
			transformer = { stack -> stack.filterNot { it == destination } },
			onComplete = { _, _ -> onComplete() }
		)

	/**
	 * Replaces the current destination with the provided one.
	 */
	fun replaceCurrent(
		withDestination: C,
		onComplete: () -> Unit = { }
	) = controller
		.replaceCurrent(withDestination) { onComplete() }

	/**
	 * Replaces all destinations with the provided one.
	 */
	fun replaceAll(
		vararg destination: C,
		onComplete: () -> Unit = { }
	) = controller
		.replaceAll(*destination) { onComplete() }
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
