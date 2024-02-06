package com.number869.decomposite.core.common.ultils.animation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.stack.animation.Direction
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.isExit
import com.arkivanov.decompose.router.stack.ChildStack


// TODO: fix predictive back somehow
abstract class OverlayOrientedAbstractStackAnimation<C : Any, T : Any>(
    private val disableInputDuringAnimation: Boolean,
) : StackAnimation<C, T> {
    @Composable
    protected abstract fun Child(
        item: AnimationItem<C, T>,
        onFinished: () -> Unit,
        content: @Composable (child: Child.Created<C, T>) -> Unit,
    )

    @Composable
    override operator fun invoke(stack: ChildStack<C, T>, modifier: Modifier, content: @Composable (child: Child.Created<C, T>) -> Unit) {
        var currentStack by remember { mutableStateOf(stack) }
        var items by remember { mutableStateOf(getAnimationItems(newStack = currentStack, oldStack = null)) }

        if (stack.active.configuration != currentStack.active.configuration) {
            val oldStack = currentStack
            currentStack = stack
            items = getAnimationItems(newStack = currentStack, oldStack = oldStack)
        }

        Box(modifier = modifier) {
            items.forEach { (configuration, item) ->
                key(configuration) {
                    Child(
                        item = item,
                        onFinished = {
                            if (item.direction.isExit) {
                                items -= configuration
                            } else {
                                items += (configuration to item)
                            }
                        },
                        content = content,
                    )
                }
            }

            // A workaround until https://issuetracker.google.com/issues/214231672.
            // Normally only the exiting child should be disabled.
            if (disableInputDuringAnimation && (items.size > 1)) {
                Overlay(modifier = Modifier.matchParentSize())
            }
        }
    }

    @Composable
    private fun Overlay(modifier: Modifier) {
        Box(
            modifier = modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        )
    }

    private fun getAnimationItems(newStack: ChildStack<C, T>, oldStack: ChildStack<C, T>?): Map<C, AnimationItem<C, T>> {
        val animationItems = mutableListOf<AnimationItem<C, T>>()

        // Add all items from the new stack as entering items.
        newStack.items.forEach { child ->
            animationItems.add(AnimationItem(child = child, direction = Direction.ENTER_FRONT))
        }

        // If there's an old stack, add all its items as exiting items.
        oldStack?.items?.forEach { child ->
            if (child !in newStack.items) {
                animationItems.add(AnimationItem(child = child, direction = Direction.EXIT_BACK))
            }
        }

        return animationItems.associateBy { it.child.configuration }
    }


    private val ChildStack<*, *>.size: Int
        get() = items.size

    private operator fun <C : Any> Iterable<Child<C, *>>.contains(config: C): Boolean =
        any { it.configuration == config }

    protected data class AnimationItem<out C : Any, out T : Any>(
        val child: Child.Created<C, T>,
        val direction: Direction,
        val isInitial: Boolean = false,
        val otherChild: Child.Created<C, T>? = null,
    )
}