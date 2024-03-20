package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.value.Value
import com.number869.decomposite.core.common.navigation.DecomposeChildInstance
import com.number869.decomposite.core.common.ultils.ImmutableThingHolder
import com.number869.decomposite.core.common.ultils.OnDestinationDisposeEffect
import kotlinx.coroutines.launch

@OptIn(InternalDecomposeApi::class)
@Composable
fun <C : Any, T : DecomposeChildInstance> StackAnimator(
    stackValue: ImmutableThingHolder<Value<ChildStack<C, T>>>,
    stackAnimatorScope: StackAnimatorScope<C>,
    modifier: Modifier = Modifier,
    onBackstackChange: (stackEmpty: Boolean) -> Unit,
    excludeStartingDestination: Boolean = false,
    allowBatchRemoval: Boolean = true,
    animations: (child: C) -> ContentAnimations,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) = with(stackAnimatorScope) {
    key(stackAnimatorScope.key) {
        val holder = rememberSaveableStateHolder()
        var sourceStack by remember { mutableStateOf(stackValue.thing.value) }
        val removingItems = remember { mutableStateListOf<C>() }
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

        LaunchedEffect(Unit) {
            // check on startup if there's animation data left for nonexistent children, which
            // can happen during a configuration change
            launch {
                removeStaleAnimationDataCache(nonStale = sourceStack.items.fastMap { it.configuration } )
            }

            stackValue.thing.subscribe { newStackRaw ->
                onBackstackChange(newStackRaw.items.size <= 1)
                val oldStack = sourceStack.items
                val newStack = newStackRaw.items.subList(
                    if (excludeStartingDestination) 1 else 0,
                    stackValue.thing.items.size
                )

                val childrenToRemove = oldStack.filter { it !in newStack && it.configuration !in removingItems }
                val batchRemoval = childrenToRemove.size > 1 && allowBatchRemoval

                // cancel removal of items that appeared again in the stack
                removingItems.removeAll(newStackRaw.items.map { it.configuration })

                if (batchRemoval) {
                    // remove from cache and everything all children, except the last one,
                    // which will be animated
                    val itemsToRemoveImmediately = childrenToRemove.subList(0, childrenToRemove.size - 1)
                    itemsToRemoveImmediately.forEach { (configuration, _) ->
                        cachedChildren.remove(configuration)
                    }
                    removingItems.add(childrenToRemove.last().configuration)
                } else {
                    childrenToRemove.forEach {
                        removingItems.add(it.configuration)
                    }
                }

                sourceStack = newStackRaw

                cachedChildren.putAll(newStack.associateBy { it.configuration })
            }
        }

        Box(modifier) {
            cachedChildren.forEach { (configuration, cachedChild) ->
                key(configuration) {
                    val inStack = !removingItems.contains(configuration)
                    val child by remember {
                        derivedStateOf {
                            sourceStack.items.find { it.configuration == configuration }
                                ?: cachedChild
                        }
                    }

                    val index = if (inStack)
                        sourceStack.items.indexOf(child)
                    else
                        -(removingItems.indexOf(configuration) + 1)

                    val indexFromTop = if (inStack)
                        sourceStack.items.size - index - 1
                    else
                        -(removingItems.indexOf(configuration) + 1)

                    val allAnimations = animations(configuration)
                    val animData = remember(allAnimations) {
                        getOrCreateAnimationData(
                            key = configuration,
                            source = allAnimations,
                            initialIndex = index,
                            initialIndexFromTop = indexFromTop
                        )
                    }

                    val allowingAnimation = indexFromTop <= (animData.renderUntils.min())

                    val animating by remember {
                        derivedStateOf {
                            animData.scopes.any { it.value.animationStatus.animating }
                        }
                    }

                    val displaying = remember(animating, allowingAnimation) {
                        val requireVisibilityInBack = animData.requireVisibilityInBackstacks.fastAny { it }
                        val renderingBack = allowingAnimation && animating
                        val renderTopAndAnimatedBack = indexFromTop < 1 || renderingBack
                        if (requireVisibilityInBack) allowingAnimation else renderTopAndAnimatedBack
                    }

                    LaunchedEffect(allowingAnimation, inStack) {
                        stackAnimatorScope.updateChildAnimPrerequisites(
                            configuration,
                            allowingAnimation,
                            inStack
                        )
                    }

                    // launch animations if there's changes
                    LaunchedEffect(indexFromTop, index) {
                        animData.scopes.forEach { (_, scope) ->
                            launch {
                                scope.update(
                                    index,
                                    indexFromTop,
                                    animate = scope.indexFromTop != indexFromTop || indexFromTop < 1
                                )

                                // after animating, if is not in stack
                                if (!inStack) cachedChildren.remove(configuration)
                            }
                        }
                    }

                    // will get triggered upon removal
                    OnDestinationDisposeEffect(
                        child.configuration.hashString() + stackAnimatorScope.key + "OnDestinationDisposeEffect",
                        waitForCompositionRemoval = true,
                        componentContext = child.instance.componentContext
                    ) {
                        removingItems.remove(configuration)
                        removeAnimationDataFromCache(configuration)
                        holder.removeState(childHolderKey(configuration))
                    }

                    if (displaying) holder.SaveableStateProvider(childHolderKey(configuration)) {
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

@OptIn(InternalDecomposeApi::class)
private fun <C : Any> childHolderKey(childConfiguration: C) =
    childConfiguration.hashString() + " StackAnimator SaveableStateHolder"
