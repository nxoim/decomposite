package com.number869.decomposite.core.common.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.items
import com.number869.decomposite.core.common.navigation.animations.ContentAnimations
import com.number869.decomposite.core.common.navigation.animations.StackAnimator
import com.number869.decomposite.core.common.navigation.animations.cleanSlideAndFade
import com.number869.decomposite.core.common.navigation.animations.emptyAnimation
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
@Composable
inline fun <reified C : Any> NavHost(
    startingNavControllerInstance: NavController<C>,
    noinline animations: (child: C) -> ContentAnimations = { cleanSlideAndFade() },
    crossinline routedContent: @Composable NavController<C>.(content: @Composable (Modifier) -> Unit) -> Unit = {
        it(Modifier)
    },
    crossinline router: @Composable (child: C) -> Unit,
) {
    with(remember { SharedBackEventScope() }) {
        var backHandlerEnabled by remember { mutableStateOf(false) }

        CompositionLocalProvider(LocalContentType provides ContentType.Contained) {
            startingNavControllerInstance.routedContent { modifier ->
                StackAnimator(
                    ImmutableThingHolder(startingNavControllerInstance.screenStack),
                    modifier,
                    key = "${C::class.simpleName} routed content",
                    sharedBackEventScope = this,
                    animations = animations,
                    onBackstackEmpty = { backHandlerEnabled = it },
                    content = {
                        CompositionLocalProvider(
                            LocalComponentContext provides it.instance.componentContext,
                            content = { router(it.configuration) }
                        )
                    }
                )
            }
        }

        LocalNavigationRoot.current.overlay {
            CompositionLocalProvider(LocalContentType provides ContentType.Overlay) {
                StackAnimator(
                    ImmutableThingHolder(startingNavControllerInstance.overlayStack),
                    onBackstackEmpty = {
                        backHandlerEnabled = if (it)
                            it
                        else
                            startingNavControllerInstance.screenStack.items.size > 1
                    },
                    key = "${C::class.simpleName} overlay content",
                    sharedBackEventScope = this,
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

            // snacks don't need to be aware of gestures
            StackAnimator(
                ImmutableThingHolder(startingNavControllerInstance.snackStack),
                onBackstackEmpty = {},
                key = "${C::class.simpleName} snack content",
                animations = {
                    startingNavControllerInstance.animationsForDestinations[it] ?: emptyAnimation()
                },
                sharedBackEventScope = SharedBackEventScope(),
                content = {
                    CompositionLocalProvider(
                        LocalComponentContext provides it.instance.componentContext,
                        content = {
                            startingNavControllerInstance.contentOfSnacks[it.configuration]?.invoke()

                            DisposableEffect(it) {
                                onDispose { startingNavControllerInstance.removeSnackContents(it.configuration) }
                            }
                        }
                    )
                }
            )
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