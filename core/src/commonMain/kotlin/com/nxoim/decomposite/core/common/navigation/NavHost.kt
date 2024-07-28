package com.nxoim.decomposite.core.common.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.items
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.DestinationAnimationsConfiguratorScope
import com.nxoim.decomposite.core.common.navigation.animations.LocalContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.StackAnimator
import com.nxoim.decomposite.core.common.navigation.animations.rememberStackAnimatorScope
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import com.nxoim.decomposite.core.common.ultils.BackGestureHandler
import com.nxoim.decomposite.core.common.ultils.ContentType
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import com.nxoim.decomposite.core.common.ultils.LocalContentType
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * Sets up stack animators for overlays and contained content and
 * manages back gestures for the animations. Animations are passed down using
 * [CompositionLocalProvider] for other navigation hosts to consume.
 *
 * [router] is a typical router where you declare the content of each destination.
 */
@NonRestartableComposable
@Composable
inline fun <reified C : Any> NavHost(
	startingNavControllerInstance: NavController<C>,
	modifier: Modifier = Modifier,
	noinline animations: DestinationAnimationsConfiguratorScope<C>.() -> ContentAnimations =
		LocalContentAnimator.current,
	crossinline router: @Composable AnimatedVisibilityScope.(destination: C) -> Unit,
) {
	val coroutineScope = rememberCoroutineScope()
	val screenStackAnimatorScope = rememberStackAnimatorScope<C>(
		"${C::class.simpleName} routed content"
	)
	val overlayStackAnimatorScope = rememberStackAnimatorScope<C>(
		"${C::class.simpleName} overlay content"
	)

	var backHandlerEnabled by rememberSaveable { mutableStateOf(false) }
	var handlingGesturesInOverlay by rememberSaveable { mutableStateOf(false) }

	CompositionLocalProvider(LocalContentType provides ContentType.Contained) {
		StackAnimator(
			stackState = startingNavControllerInstance.screenStack.subscribeAsState(),
			stackAnimatorScope = screenStackAnimatorScope,
			modifier = modifier,
			animations = animations,
			onBackstackChange = { empty -> backHandlerEnabled = !empty },
			content = {
				CompositionLocalProvider(
					LocalComponentContext provides it.instance.componentContext,
					LocalContentAnimator provides animations as DestinationAnimationsConfiguratorScope<*>.() -> ContentAnimations,
					content = { router(it.configuration) }
				)
			}
		)
	}

	LocalNavigationRoot.current.overlay {
		CompositionLocalProvider(LocalContentType provides ContentType.Overlay) {
			StackAnimator(
				stackState = startingNavControllerInstance.overlayStack.subscribeAsState(),
				stackAnimatorScope = overlayStackAnimatorScope,
				onBackstackChange = { empty ->
					handlingGesturesInOverlay = !empty
					if (empty) coroutineScope.coroutineContext.cancelChildren()
					backHandlerEnabled = if (empty)
						startingNavControllerInstance.screenStack.items.size > 1
					else
						true
				},
				animations = animations,
				excludeStartingDestination = true,
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

	BackGestureHandler(
		enabled = backHandlerEnabled,
		startingNavControllerInstance.backHandler,
		onBackStarted = {
			coroutineScope.launch {
				if (handlingGesturesInOverlay) {
					overlayStackAnimatorScope.updateGestureDataInScopes(
						BackGestureEvent.OnBackStarted(it)
					)
				} else {
					screenStackAnimatorScope.updateGestureDataInScopes(
						BackGestureEvent.OnBackStarted(it)
					)
				}
			}
		},
		onBackProgressed = {
			coroutineScope.launch {
				if (handlingGesturesInOverlay) {
					overlayStackAnimatorScope.updateGestureDataInScopes(
						BackGestureEvent.OnBackProgressed(it)
					)
				} else {
					screenStackAnimatorScope.updateGestureDataInScopes(
						BackGestureEvent.OnBackProgressed(it)
					)
				}
			}
		},
		onBackCancelled = {
			coroutineScope.launch {
				if (handlingGesturesInOverlay) {
					overlayStackAnimatorScope
						.updateGestureDataInScopes(BackGestureEvent.OnBackCancelled)
				} else {
					screenStackAnimatorScope
						.updateGestureDataInScopes(BackGestureEvent.OnBackCancelled)
				}
			}
		},
		onBack = {
			startingNavControllerInstance.navigateBack()
			coroutineScope.launch {
				if (handlingGesturesInOverlay) {
					overlayStackAnimatorScope
						.updateGestureDataInScopes(BackGestureEvent.OnBack)
				} else {
					screenStackAnimatorScope
						.updateGestureDataInScopes(BackGestureEvent.OnBack)
				}
			}
		}
	)
}