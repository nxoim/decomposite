package com.nxoim.decomposite.core.common.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.items
import com.nxoim.decomposite.core.common.navigation.animations.*
import com.nxoim.decomposite.core.common.ultils.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * Navigation Host.
 * [router] is where you declare the content of each destination.
 */
@NonRestartableComposable
@Composable
inline fun <reified C : Any> NavHost(
    startingNavControllerInstance: NavController<C>,
    modifier: Modifier = Modifier,
    noinline animations: AnimatorChildrenConfigurations<C>.() -> ContentAnimations = { cleanSlideAndFade() },
    crossinline router: @Composable (child: C) -> Unit,
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
            stackValue = ImmutableThingHolder(startingNavControllerInstance.screenStack),
            stackAnimatorScope = screenStackAnimatorScope,
            modifier = modifier,
            animations = animations,
            onBackstackChange = { empty -> backHandlerEnabled = !empty },
            content = {
                CompositionLocalProvider(
                    LocalComponentContext provides it.instance.componentContext,
                    content = { router(it.configuration) }
                )
            }
        )
    }

    LocalNavigationRoot.current.overlay {
        CompositionLocalProvider(LocalContentType provides ContentType.Overlay) {
            StackAnimator(
                stackValue = ImmutableThingHolder(startingNavControllerInstance.overlayStack),
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
                    overlayStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBackStarted(it))
                } else {
                    screenStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBackStarted(it))
                }
            }
        },
        onBackProgressed = {
            coroutineScope.launch {
                if (handlingGesturesInOverlay) {
                    overlayStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBackProgressed(it))
                } else {
                    screenStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBackProgressed(it))
                }
            }
        },
        onBackCancelled = {
            coroutineScope.launch {
                if (handlingGesturesInOverlay) {
                    overlayStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBackCancelled)
                } else {
                    screenStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBackCancelled)
                }
            }
        },
        onBack = {
            startingNavControllerInstance.navigateBack()
            coroutineScope.launch {
                if (handlingGesturesInOverlay) {
                    overlayStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBack)
                } else {
                    screenStackAnimatorScope.updateGestureDataInScopes(BackGestureEvent.OnBack)
                }
            }
        }
    )
}