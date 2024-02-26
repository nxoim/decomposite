package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.number869.decomposite.core.common.ultils.SharedBackEventScope

@OptIn(InternalDecomposeApi::class)
@Composable
fun <C : Any, T : Any> CustomStackAnimator(
    stackState: Value<ChildStack<C, T>>,
    modifier: Modifier = Modifier,
    onBackstackEmpty: (Boolean) -> Unit,
    excludeStartingDestination: Boolean = false,
    sharedBackEventScope: SharedBackEventScope,
    content: @Composable NavigationItem.(child: Child.Created<C, T>) -> Unit,
) {
    val sourceStack by stackState.subscribeAsState()
    var cachedChildren by remember { mutableStateOf(listOf(stackState.value.active)) }

    val holder = rememberSaveableStateHolder()
    holder.retainStates(sourceStack.getConfigurations())

    LaunchedEffect(sourceStack.items, sourceStack.active) {
        onBackstackEmpty(sourceStack.items.size > 1)

        val differences = sourceStack.items.filterNot { it in cachedChildren }
        cachedChildren = cachedChildren + differences
    }

    Box(modifier) {
        val children = if (excludeStartingDestination)
            cachedChildren.filterNot { it == sourceStack.items.first() }
        else
            cachedChildren

        children.forEach { child ->
            val inStack = sourceStack.items.contains(child)
            val index = if (inStack) sourceStack.items.indexOf(child) else -1
            val indexFromTop = if (inStack) sourceStack.items.reversed().indexOf(child) else -1

            holder.SaveableStateProvider(child.hashString()) {
                val navigationItem = remember {
                    NavigationItem(index, indexFromTop, sharedBackEventScope)
                }

                // launch animations if there's changes
                LaunchedEffect(sourceStack.items, indexFromTop) {
                    navigationItem.updateIndex(index, indexFromTop)
                }

                LaunchedEffect(navigationItem.requestedRemoval) {
                    if (navigationItem.requestedRemoval == true)
                        cachedChildren = cachedChildren - child
                }

                Box(Modifier.zIndex((-indexFromTop).toFloat())) { content(navigationItem, child) }
            }
        }
    }
}

@OptIn(InternalDecomposeApi::class)
private fun ChildStack<*, *>.getConfigurations() = items.mapTo(HashSet()) { it.configuration.hashString() }

@Composable
private fun SaveableStateHolder.retainStates(currentKeys: Set<Any>) {
    val keys = remember(this) { Keys(currentKeys) }

    DisposableEffect(this, currentKeys) {
        keys.set.forEach { if (it !in currentKeys) removeState(it) }

        keys.set = currentKeys

        onDispose {}
    }
}

private class Keys(var set: Set<Any>)