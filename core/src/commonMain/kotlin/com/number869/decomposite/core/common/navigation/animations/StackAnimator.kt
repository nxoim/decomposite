package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.value.Value
import com.number869.decomposite.core.common.navigation.DecomposeChildInstance
import com.number869.decomposite.core.common.ultils.BackGestureEvent
import com.number869.decomposite.core.common.ultils.ImmutableThingHolder
import com.number869.decomposite.core.common.ultils.SharedBackEventScope
import com.number869.decomposite.core.common.ultils.rememberRetained
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(InternalDecomposeApi::class)
@Composable
fun <C : Any, T : DecomposeChildInstance> StackAnimator(
    stackValue: ImmutableThingHolder<Value<ChildStack<C, T>>>,
    modifier: Modifier = Modifier,
    key: String = "",
    onBackstackEmpty: (Boolean) -> Unit,
    excludeStartingDestination: Boolean = false,
    sharedBackEventScope: SharedBackEventScope,
    animations: (child: C) -> ContentAnimations,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sourceStack by stackValue.thing.subscribeAsState()
    // it's important to use the local instance keeper rather than of the children's for the
    // scopes not to be recreated which is useful in case the exit animation of a config is
    // interrupted by the same config appearing in the stack again while the animation is running
    val stackAnimatorScope = rememberRetained(key + "StackAnimatorScope") {
        StackAnimatorScope<C>()
    }
    var cachedChildren by remember {
        mutableStateOf(
            stackValue.thing.items.subList(
                if (excludeStartingDestination) 1 else 0,
                stackValue.thing.items.size
            )
        )
    }
    val mutex = remember { Mutex() }

    with(stackAnimatorScope) {
        LaunchedEffect(sourceStack.items) {
            onBackstackEmpty(sourceStack.items.size > 1)

            val differences = stackValue.thing.items
                .subList(
                    if (excludeStartingDestination) 1 else 0,
                    stackValue.thing.items.size
                )
                .filterNot { it in cachedChildren }
            mutex.withLock { cachedChildren += differences }
        }

        Box(modifier) {
            cachedChildren.fastForEach { cachedChild ->
                val configuration = cachedChild.configuration
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

                    val animating = animData.scopes.fastAny { it.animationStatus.animating }

                    val render = remember(animating) {
                        val requireVisibilityInBack = animData.requireVisibilityInBackstacks.fastAny { it }
                        val renderingBack = allowAnimation && animating
                        val renderTopAndAnimatedBack = indexFromTop < 1 || renderingBack
                        if (requireVisibilityInBack) allowAnimation else renderTopAndAnimatedBack
                    }

                    // launch animations if there's changes
                    LaunchedEffect(indexFromTop, index) {
                        coroutineScope.launch {
                            animData.scopes.fastForEach { scope ->
                                launch {
                                    scope.updateCurrentIndexAndAnimate(
                                        index,
                                        indexFromTop,
                                        animate = scope.indexFromTop != indexFromTop || indexFromTop < 1
                                    )

                                    if (!inStack) { // after animating, if is not in stack
                                        mutex.withLock {
                                            cachedChildren -= child
                                            removeFromCache(configuration)
                                            holder.removeState(childHolderKey)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(null) {
                        if (allowAnimation) launch {
                            sharedBackEventScope.gestureActions.collectLatest() {
                                // wrapped in run catching because sometimes it can happen
                                // that attempts to update the gesture data in nonexistent scopes
                                // will be made, which will throw an error
                                runCatching { updateGestureDataInScopes(configuration, it) }
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

@Immutable
private class StackAnimatorScope<C : Any>() {
    private val animationDataRegistry = AnimationDataRegistry<C>()

    inline fun getOrCreateAnimationData(key: C, source: ContentAnimations, initialIndex: Int, initialIndexFromTop: Int) =
        animationDataRegistry.getOrCreateAnimationData(key, source, initialIndex, initialIndexFromTop)

    inline fun removeFromCache(target: C) { animationDataRegistry.remove(target) }

    suspend fun updateGestureDataInScopes(
        target: C,
        backGestureData: BackGestureEvent
    ) = coroutineScope {
        animationDataRegistry.get(target).scopes.fastForEach {
            launch { it.onBackGesture(backGestureData) }
        }
    }

    fun Modifier.accumulate(modifiers: List<Modifier>) = modifiers.fold(this) { acc, modifier ->
        acc.then(modifier)
    }
}

data class AnimationData(
    val scopes: List<ContentAnimatorScope>,
    val modifiers: List<Modifier>,
    val renderUntils: List<Int>,
    val requireVisibilityInBackstacks: List<Boolean>,
)
private class AnimationDataRegistry<C : Any> () {
    private val animationScopeRegistry = AnimationScopeRegistry()
    private val animationData = hashMapOf<String, AnimationData>()

    @OptIn(InternalDecomposeApi::class)
    inline fun getOrCreateAnimationData(
        key: C,
        source: ContentAnimations,
        initialIndex: Int,
        initialIndexFromTop: Int
    ) = animationData[key.hashString()] ?: animationData.getOrPut(key.hashString()) {
        val scopes = source.items.fastMap {
            // this will also automatically combine all scopes with the same
            // animation specs, meaning the minimum amount of scopes required
            // will be created
            animationScopeRegistry.getOrPut(it.animationSpec.hashString() + key.hashString() + "animator scope") {
                DefaultContentAnimatorScope(initialIndex, initialIndexFromTop, it.animationSpec)
            }
        }

        AnimationData(
            scopes = scopes,
            modifiers = source.items.fastMapIndexed { animIndex, it ->
                it.animationModifier.invoke(scopes[animIndex])
            },
            renderUntils = source.items.fastMap { it.renderUntil },
            requireVisibilityInBackstacks = source.items.fastMap { it.requireVisibilityInBackstack },
        )
    }

    @OptIn(InternalDecomposeApi::class)
    fun get(key: C) = animationData[key.hashString()] ?: error("No animation data for $key in AnimationDataRegistry")

    @OptIn(InternalDecomposeApi::class)
    fun remove(key: C) {
        animationData.remove(key.hashString())
        animationScopeRegistry.remove(key.hashString())
    }
}

private class AnimationScopeRegistry {
    private val scopes = hashMapOf<String, ContentAnimatorScope>()

    inline fun getOrPut(key: String, scope: () -> ContentAnimatorScope) = scopes.getOrPut(key, scope)
    inline fun remove(key: String) {
        val childrensScopes = scopes.filter { it.key.contains(key) }
        childrensScopes.keys.forEach { scopes.remove(it) }
    }
}
