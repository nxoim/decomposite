package com.number869.decomposite.core.common.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.items
import com.number869.decomposite.core.common.navigation.animations.*
import com.number869.decomposite.core.common.ultils.*

/**
 * Navigation Host.
 * [router] is where you declare the content of each destination.
 * [routedContent] is where the content is displayed, where you put your scaffold, maybe something else.
 * When a host is created - a navigation controller is created for it, and it is accessible in [routedContent],
 * however you can make your own controller by calling [navController] and giving it a starting destination, or
 * manually by requesting [LocalNavControllerStore] and calling [NavControllerStore.getOrCreate]
 * with the type of your destination.
 */
@Stable
@Composable
inline fun <reified C : Any> NavHost(
    startingNavControllerInstance: NavController<C>,
    defaultAnimation: ContentAnimator = cleanSlideAndFade(),
    crossinline routedContent: @Composable NavController<C>.(content: @Composable (Modifier) -> Unit) -> Unit = { it(Modifier) },
    crossinline router: @Composable NavigationItem.(child: C) -> Unit
) {
    with(remember { SharedBackEventScope() }) {
        var backHandlerEnabled by remember { mutableStateOf(false) }

        CompositionLocalProvider(LocalContentAnimator provides defaultAnimation) {
            CompositionLocalProvider(LocalContentType provides ContentType.Contained) {
                startingNavControllerInstance.routedContent { modifier ->
                    CustomStackAnimator(
                        startingNavControllerInstance.screenStack,
                        modifier,
                        sharedBackEventScope = this,
                        onBackstackEmpty = { backHandlerEnabled = it }
                    ) {
                        CompositionLocalProvider(
                            LocalComponentContext provides it.instance.componentContext
                        ) {
                            router(it.configuration)
                        }
                    }
                }
            }

            LocalNavigationRoot.current.overlay {
                CompositionLocalProvider(LocalContentType provides ContentType.Overlay) {
                    CustomStackAnimator(
                        startingNavControllerInstance.overlayStack,
                        onBackstackEmpty = {
                            backHandlerEnabled = if (it)
                                it
                            else
                                startingNavControllerInstance.screenStack.items.size > 1
                        },
                        sharedBackEventScope = this,
                        excludeStartingDestination = true
                    ) {
                        CompositionLocalProvider(
                            LocalComponentContext provides it.instance.componentContext
                        ) {
                            runCatching { router(it.configuration) }
                        }
                    }
                }

                // snacks dont need to be aware of gestures
                CustomStackAnimator(
                    startingNavControllerInstance.snackStack,
                    onBackstackEmpty = {},
                    sharedBackEventScope = SharedBackEventScope(),
                ) {
                    CompositionLocalProvider(LocalComponentContext provides it.instance.componentContext) {
                        animatedDestination(
                            startingNavControllerInstance.animationsForDestinations[it.configuration] ?: emptyAnimation()
                        ) {
                            startingNavControllerInstance.contentOfSnacks[it.configuration]?.invoke()

                            DisposableEffect(it) {
                                onDispose { startingNavControllerInstance.removeSnackContents(it.configuration) }
                            }
                        }
                    }
                }
            }
        }

        BackGestureHandler(
            enabled = backHandlerEnabled,
            startingNavControllerInstance.backHandler,
            onBackStarted = { onBackStarted(it) },
            onBackProgressed = { onBackProgressed(it) },
            onBackCancelled = { onBackCancelled() },
            onBack = {
                startingNavControllerInstance.navigateBack()
                onBack()
            }
        )
    }
}

@Composable
inline fun <reified C : Any> NavHost(
    startingDestination: C,
    defaultAnimation: ContentAnimator = cleanSlideAndFade(),
    crossinline routedContent: @Composable NavController<C>.(content: @Composable (Modifier) -> Unit) -> Unit = { it(Modifier) },
    crossinline router: @Composable NavigationItem.(child: C) -> Unit
) {
    NavHost(
        startingNavControllerInstance = navController<C>(startingDestination),
        defaultAnimation = defaultAnimation,
        routedContent = routedContent,
        router = router
    )
}

@Composable
inline fun <reified C : Any> NavHost(
    startingDestination: C,
    crossinline animations: NavigationItem.(child: C) -> ContentAnimator,
    crossinline routedContent: @Composable NavController<C>.(content: @Composable (Modifier) -> Unit) -> Unit = { it(Modifier) },
    crossinline router: @Composable (child: C) -> Unit
) {
    NavHost(
        startingNavControllerInstance = navController<C>(startingDestination),
        routedContent = routedContent,
    ) {
        animatedDestination(animations(it)) { router(it) }
    }
}

@Composable
inline fun <reified C : Any> NavHost(
    startingNavControllerInstance: NavController<C>,
    crossinline animations: NavigationItem.(child: C) -> ContentAnimator,
    crossinline routedContent: @Composable NavController<C>.(content: (Modifier) -> Unit) -> Unit = { it(Modifier) },
    crossinline router: @Composable (child: C) -> Unit
) {
    NavHost(
        startingNavControllerInstance = startingNavControllerInstance,
        routedContent = routedContent,
    ) {
        animatedDestination(animations(it)) { router(it) }
    }
}