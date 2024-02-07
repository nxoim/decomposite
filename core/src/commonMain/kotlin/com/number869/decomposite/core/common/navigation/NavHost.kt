package com.number869.decomposite.core.common.navigation

import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.*
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.router.stack.backStack
import com.number869.decomposite.core.common.ultils.ContentType
import com.number869.decomposite.core.common.ultils.LocalComponentContext
import com.number869.decomposite.core.common.ultils.LocalContentType
import com.number869.decomposite.core.common.ultils.animation.OverlayStackNavigationAnimation
import kotlinx.serialization.serializer

/**
 * Navigation Host.
 * [router] is where you declare the content of each destination.
 * [routedContent] is where the content is displayed, where you put your scaffold, maybe something else.
 * When a host is created - a navigation controller is created for it, and it is accessible in [routedContent],
 * however you can make your own controller by requesting [LocalNavControllerStore]
 * and calling [NavControllerStore.getOrCreate] with the type of your destination.
 *
 *
 * You can provide your own animations for the content and overlaying content.
 * [containedContentAnimation] and [overlayingContentAnimation] are decompose animations.
 */
@OptIn(ExperimentalDecomposeApi::class)
@Stable
@Composable
inline fun <reified C : Any> NavHost(
    startingDestination: C,
    navControllerStore: NavControllerStore = LocalNavControllerStore.current,
    parentsComponentContext: ComponentContext = LocalComponentContext.current,
    startingNavControllerInstance: NavController<C> = remember {
        navControllerStore.getOrCreate {
            NavController(startingDestination, serializer(), parentsComponentContext)
        }
    },
    crossinline containedContentAnimation: NavController<C>.() -> StackAnimation<C, DecomposeChildInstance<C>> = { stackAnimation(scale() + fade()) },
    crossinline overlayingContentAnimation: NavController<C>.() -> StackAnimation<C, DecomposeChildInstance<C>> = {
        predictiveBackAnimation(
            backHandler = parentsComponentContext::backHandler.get(),
            onBack = { startingNavControllerInstance.navigateBack() },
            fallbackAnimation = OverlayStackNavigationAnimation { _ ->
                fade(tween(200)) + scale(tween(200))
            }
        )
    },
    crossinline routedContent: @Composable NavController<C>.(content: @Composable (Modifier) -> Unit) -> Unit = { it(Modifier) },
    crossinline router: @Composable (destination: C) -> Unit
) {
    startingNavControllerInstance.routedContent { modifier ->
        Children(
            startingNavControllerInstance.screenStack,
            modifier,
            startingNavControllerInstance.containedContentAnimation()
        ) {
            if (it != startingNavControllerInstance.overlayStack.backStack) CompositionLocalProvider(
                LocalComponentContext provides it.instance.componentContext,
                LocalContentType provides ContentType.Contained,
                content = { router(it.configuration) }
            )
        }
    }

    LocalNavigationRoot.current.overlay {
        Children(
            startingNavControllerInstance.overlayStack,
            Modifier,
            startingNavControllerInstance.overlayingContentAnimation()
        ) { child ->
            CompositionLocalProvider(
                LocalComponentContext provides child.instance.componentContext,
                LocalContentType provides ContentType.Overlay
            ) {
                if (child.configuration != startingDestination) router(child.configuration)
            }
        }

        Children(
            startingNavControllerInstance.snackStack,
            animation = stackAnimation { child ->
                startingNavControllerInstance.animationsForDestinations[child.configuration]
            }
        ) {
            CompositionLocalProvider(
                LocalComponentContext provides it.instance.componentContext
            ) {
                startingNavControllerInstance.contentOfSnacks[it.configuration]?.invoke()

                DisposableEffect(it) {
                    onDispose { startingNavControllerInstance.removeSnackContents(it.configuration) }
                }
            }
        }
    }
}