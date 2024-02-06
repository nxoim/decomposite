package com.number869.decomposite.core.common.ultils.animation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.stack.animation.Direction
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator

class OverlayStackNavigationAnimation<C : Any, T : Any>(
    disableInputDuringAnimation: Boolean = false,
    private val selector: (Child.Created<C, T>) -> StackAnimator?,
) : OverlayOrientedAbstractStackAnimation<C, T>(disableInputDuringAnimation = disableInputDuringAnimation) {
    @Composable
    override fun Child(
        item: AnimationItem<C, T>,
        onFinished: () -> Unit,
        content: @Composable (child: Child.Created<C, T>) -> Unit,
    ) {
        val animator = remember(item.child.configuration) {
            selector(item.child) ?: EmptyStackAnimator
        }

        animator(
            direction = item.direction,
            isInitial = item.isInitial,
            onFinished = onFinished,
        ) { modifier ->
            Box(modifier) { content(item.child) }
        }
    }
}

internal object EmptyStackAnimator : StackAnimator {
    @Composable
    override fun invoke(
        direction: Direction,
        isInitial: Boolean,
        onFinished: () -> Unit,
        content: @Composable (Modifier) -> Unit,
    ) {
        content(Modifier)

        DisposableEffect(direction, isInitial) {
            onFinished()
            onDispose { }
        }
    }
}