package com.nxoim.decomposite.core.common.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastMap
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nxoim.decomposite.core.common.navigation.animations.AnimationDataRegistry
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.DestinationAnimationsConfiguratorScope
import com.nxoim.decomposite.core.common.navigation.animations.LocalContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.StackAnimator
import com.nxoim.decomposite.core.common.navigation.animations.rememberStackAnimatorScope
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import com.nxoim.decomposite.core.common.ultils.BackGestureHandler
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import kotlinx.coroutines.Dispatchers
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
	val animationsCoroutineScope = rememberCoroutineScope() { Dispatchers.Default }

	var backHandlerEnabled by rememberSaveable { mutableStateOf(false) }

	val stack by startingNavControllerInstance.screenStack.subscribeAsState()

	val screenStackAnimatorScope = rememberStackAnimatorScope(
		"${startingNavControllerInstance.key} routed content",
		stack = { stack.items },
		itemKey = { it.configuration },
		excludedDestinations = { excludedDestinations?.contains(it.configuration) == true },
		animations = {
			animations(
				DestinationAnimationsConfiguratorScope(
					previousChild = previousChild?.configuration,
					currentChild = currentChild.configuration,
					nextChild = nextChild?.configuration,
					exitingChildren = exitingChildren.fastMap { it.configuration },
					screenInformation = screenInformation
				)
			)
		},
		onBackstackChange = { empty -> backHandlerEnabled = !empty },
		animationDataRegistry = remember { AnimationDataRegistry() }
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

	BackGestureHandler(
		enabled = backHandlerEnabled,
		startingNavControllerInstance.backHandler,
		onBackStarted = {
			animationsCoroutineScope.launch {
				screenStackAnimatorScope.updateGestureDataInScopes(
					BackGestureEvent.OnBackStarted(it)
				)
			}
		},
		onBackProgressed = {
			animationsCoroutineScope.launch {
				screenStackAnimatorScope.updateGestureDataInScopes(
					BackGestureEvent.OnBackProgressed(it)
				)
			}
		},
		onBackCancelled = {
			animationsCoroutineScope.launch {
				screenStackAnimatorScope
					.updateGestureDataInScopes(BackGestureEvent.OnBackCancelled)
			}
		},
		onBack = {
			startingNavControllerInstance.navigateBack()

			animationsCoroutineScope.launch {
				screenStackAnimatorScope
					.updateGestureDataInScopes(BackGestureEvent.OnBack)
			}
		}
	)
}