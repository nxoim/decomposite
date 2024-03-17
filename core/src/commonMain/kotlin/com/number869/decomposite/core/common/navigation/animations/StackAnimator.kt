package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.value.Value
import com.number869.decomposite.core.common.navigation.DecomposeChildInstance
import com.number869.decomposite.core.common.ultils.ImmutableThingHolder
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(InternalDecomposeApi::class)
@Composable
fun <C : Any, T : DecomposeChildInstance> StackAnimator(
    stackValue: ImmutableThingHolder<Value<ChildStack<C, T>>>,
    stackAnimatorScope: StackAnimatorScope<C>,
    modifier: Modifier = Modifier,
    onBackstackChange: (stackEmpty: Boolean) -> Unit,
    excludeStartingDestination: Boolean = false,
    animations: (child: C) -> ContentAnimations,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sourceStack by stackValue.thing.subscribeAsState()

    val cachedChildren = remember {
        mutableStateMapOf<C, Child.Created<C, T>>().apply {
            putAll(
                stackValue.thing.items.subList(
                    if (excludeStartingDestination) 1 else 0,
                    stackValue.thing.items.size
                ).associateBy { it.configuration }
            )
        }
    }
    val mutex = remember { Mutex() }

    with(stackAnimatorScope) {
        LaunchedEffect(sourceStack.items) {
            onBackstackChange(sourceStack.items.size == 1)

            val differences = stackValue.thing.items
                .subList(
                    if (excludeStartingDestination) 1 else 0,
                    stackValue.thing.items.size
                )
                .filterNot {
                    // also check if the instance is equal
                    it.configuration in cachedChildren || it.instance == cachedChildren[it.configuration]?.instance
                }
            mutex.withLock { cachedChildren.putAll(differences.associateBy { it.configuration }) }
        }

        Box(modifier) {
            cachedChildren.forEach { (configuration, cachedChild) ->
                val holder = rememberSaveableStateHolder()
                val childHolderKey = configuration.hashString() + " StackAnimator SaveableStateHolder"

                key(configuration) {
                    val inStack = sourceStack.items.fastAny { it.configuration == configuration }
                    val child by remember {
                        derivedStateOf {
                            sourceStack.items.find { it.configuration == configuration }
                                ?: cachedChild
                        }
                    }

                    val index = if (inStack) sourceStack.items.indexOf(child) else -1
                    val indexFromTop = if (inStack)
                        sourceStack.items.reversed().indexOf(child)
                    else
                        -1

                    val animData = remember {
                        getOrCreateAnimationData(
                            key = child.configuration,
                            source = animations(configuration),
                            initialIndex = index,
                            initialIndexFromTop = indexFromTop
                        )
                    }

                    val allowAnimation = indexFromTop <= (animData.renderUntils.min())

                    val animating = animData.scopes.any { it.value.animationStatus.animating }

                    val render = remember(animating) {
                        val requireVisibilityInBack = animData.requireVisibilityInBackstacks.fastAny { it }
                        val renderingBack = allowAnimation && animating
                        val renderTopAndAnimatedBack = indexFromTop < 1 || renderingBack
                        if (requireVisibilityInBack) allowAnimation else renderTopAndAnimatedBack
                    }

                    LaunchedEffect(allowAnimation, inStack) {
                        stackAnimatorScope.updateChildAnimPrerequisites(
                            configuration,
                            allowAnimation,
                            inStack
                        )
                    }

                    // launch animations if there's changes
                    LaunchedEffect(indexFromTop, index) {
                        coroutineScope.launch {
                            animData.scopes.forEach { (_, scope) ->
                                launch {
                                    scope.update(
                                        index,
                                        indexFromTop,
                                        animate = scope.indexFromTop != indexFromTop || indexFromTop < 1
                                    )

                                    if (!inStack) { // after animating, if is not in stack
                                        mutex.withLock {
                                            cachedChildren.remove(configuration)
                                            removeFromCache(configuration)
                                            holder.removeState(childHolderKey)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (render) holder.SaveableStateProvider(childHolderKey) {
                        Box(
                            Modifier.zIndex((-indexFromTop).toFloat()).accumulate(animData.modifiers),
                            content = { content(child) }
                        )
                    }
                }
            }
        }
    }
}


