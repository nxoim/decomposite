package com.nxoim.decomposite.core.common.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import com.nxoim.decomposite.core.common.ultils.BackGestureHandler
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Sets up stack animators for overlays and contained content and
 * manages back gestures for the animations. Animations are passed down using
 * [CompositionLocalProvider] for other navigation hosts to consume.
 *
 * [router] is a typical router where you declare the content of each destination.
 *
 * @param excludedDestinations allows to specify what destinations should not be
 * rendered and animated.
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
		var backHandlerEnabled by rememberSaveable { mutableStateOf(false) }

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
				itemKey = { it.key },
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
				onBackstackChange = { empty -> backHandlerEnabled = !empty },
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
			enabled = backHandlerEnabled,
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
	val animationsCoroutineScope = rememberCoroutineScope()

	BackGestureHandler(
		enabled = enabled,
		backHandler,
		onBackStarted = {
			animationsCoroutineScope.launch {
				stackAnimatorScope.updateGestureDataInScopes(
					BackGestureEvent.OnBackStarted(it)
				)
			}
		},
		onBackProgressed = {
			animationsCoroutineScope.launch {
				stackAnimatorScope.updateGestureDataInScopes(
					BackGestureEvent.OnBackProgressed(it)
				)
			}
		},
		onBackCancelled = {
			animationsCoroutineScope.launch {
				stackAnimatorScope.updateGestureDataInScopes(
					BackGestureEvent.OnBackCancelled
				)
			}
		},
		onBack = {
			animationsCoroutineScope.launch {
				onBack()

				stackAnimatorScope.updateGestureDataInScopes(
					BackGestureEvent.OnBack
				)
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