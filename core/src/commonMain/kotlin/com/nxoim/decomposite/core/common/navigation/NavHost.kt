package com.nxoim.decomposite.core.common.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastMap
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.DestinationAnimationsConfiguratorScope
import com.nxoim.decomposite.core.common.navigation.animations.LocalContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.stack.AnimationDataRegistry
import com.nxoim.decomposite.core.common.navigation.animations.stack.StackAnimator
import com.nxoim.decomposite.core.common.navigation.animations.stack.StackAnimatorScope
import com.nxoim.decomposite.core.common.navigation.animations.stack.rememberStackAnimatorScope
import com.nxoim.decomposite.core.common.ultils.BackGestureHandler
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

/**
 * Sets up a stack animator and manages back gestures for the animations.
 * Provides each destination with [ComponentContext] via [LocalComponentContext].
 * Animations are passed down using [CompositionLocalProvider] for other NavHosts to consume.
 *
 * **Example:**
 *
 * ```kotlin
 * @Composable
 * fun MyScreen(navController: NavController<String>) {
 *     NavHost(
 * 	       startingNavControllerInstance = navController,
 * 		   animations = { cleanSlideAndFade() }
 * 	    ) { destination ->
 * 		    when (destination) {
 * 			    // handle destinations here
 * 		   }
 * 	    }
 * }
 * ```
 *
 * @param startingNavControllerInstance The [NavController] instance for the navigation host.
 * @param modifier An optional modifier for the host container.
 * @param excludedDestinations A list of destinations that should not be rendered or animated.
 * @param animations A lambda configuring the animations for the stack. Defaults to `cleanSlideAndFade()`.
 * @param router A composable function that receives the current destination and renders the corresponding content.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun <C : Any> NavHost(
	startingNavControllerInstance: NavController<C>,
	modifier: Modifier = Modifier,
	excludedDestinations: List<C>? = null,
	animations: DestinationAnimationsConfiguratorScope<C>.() -> ContentAnimations =
		LocalContentAnimator.current,
	router: @Composable AnimatedVisibilityScope.(destination: C) -> Unit,
) {
	// a key is needed because theres some caching issues with animations
	// when the nav hosts are nested because of forEach loops in stack animator.
	// maybe related to currentCompositeKeyHash?
	key(startingNavControllerInstance.key) {
		val stack by startingNavControllerInstance.screenStack.subscribeAsState()

		// this is wrapped in the key because the entire thing needs to
		// recompose when the nav controller instance gets updated,
		// which usually happens when the component context changes
		//
		// without the key, if the user comes back to the destination that's
		// in the process of being removed, the provided instance in the
		// stack animator is incorrect, breaking navigation in the destination
		// until the user closes the destination, waits for the animation to end,
		// and reopens it
		//
		// im not sure why stack.items in the lambda doesn't get updated
		// during that change
		val screenStackAnimatorScope = key(startingNavControllerInstance) {
			rememberStackAnimatorScope(
				stack = { stack.items },
				itemKey = { it.configuration },
				excludedDestinations = { excludedDestinations?.contains(it.configuration) == true },
				animations = {
					animations(
						DestinationAnimationsConfiguratorScope(
							previousChild = previousChild?.configuration,
							currentChild = currentChild.configuration,
							nextChild = nextChild?.configuration,
							exitingChildren = { exitingChildren().fastMap { it.configuration } },
						)
					)
				},
				animationDataRegistry = createAnimationDataRegistry(
					startingNavControllerInstance.key,
					startingNavControllerInstance.parentComponentContext
				)
			)
		}


		HandleBackGesturesForStackAnimations(
			stackAnimatorScope = screenStackAnimatorScope,
			backHandler = startingNavControllerInstance
				.parentComponentContext
				.backHandler,
			enabled = stack.items.size > 1,
			onBack = startingNavControllerInstance::navigateBack
		)

		StackAnimator(
			stackAnimatorScope = screenStackAnimatorScope,
			modifier = modifier,
			content = {
				CompositionLocalProvider(
					LocalComponentContext provides it.instance.componentContext,
					LocalContentAnimator provides animations as DestinationAnimationsConfiguratorScope<*>.() -> ContentAnimations,
					content = { router(it.configuration) }
				)
			}
		)
	}
}

@Composable
private fun HandleBackGesturesForStackAnimations(
	stackAnimatorScope: StackAnimatorScope<*, *>,
	backHandler: BackHandler,
	enabled: Boolean,
	onBack: () -> Unit
) {
	val channel = remember { Channel<suspend () -> Unit>(capacity = CONFLATED) }

	LaunchedEffect(Unit) {
		withContext(currentCoroutineContext() + SupervisorJob()) {
			for (action in channel) { action.invoke() }
		}
	}

	BackGestureHandler(
		enabled = enabled,
		backHandler,
		onBackStarted = {
			channel.trySend {
				stackAnimatorScope
					.gestureUpdateHandler
					.dispatchOnStart(it)
			}
		},
		onBackProgressed = {
			channel.trySend {
				stackAnimatorScope
					.gestureUpdateHandler
					.dispatchOnProgressed(it)
			}
		},
		onBackCancelled = {
			channel.trySend {
				stackAnimatorScope
					.gestureUpdateHandler
					.dispatchOnCancelled()
			}
		},
		onBack = {
			channel.trySend {
				onBack()

				stackAnimatorScope
					.gestureUpdateHandler
					.dispatchOnCompleted()
			}
		}
	)
}

@Composable
private fun <Key : Any> createAnimationDataRegistry(
	key: Any,
	componentContext: ComponentContext = LocalComponentContext.current
) = remember(componentContext, key) {
	componentContext
		.instanceKeeper
		.getOrCreateSimple("animation data registry$key") { AnimationDataRegistry<Key>() }
}