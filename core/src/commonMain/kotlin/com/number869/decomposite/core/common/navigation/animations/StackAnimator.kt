package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.*
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
fun <C : Any> StackAnimator(
    stackValue: ImmutableThingHolder<Value<ChildStack<C, DecomposeChildInstance<C>>>>,
    modifier: Modifier = Modifier,
    onBackstackEmpty: (Boolean) -> Unit,
    excludeStartingDestination: Boolean = false,
    sharedBackEventScope: SharedBackEventScope,
    animations: (child: C) -> ContentAnimations,
    content: @Composable (child: Child.Created<C, DecomposeChildInstance<C>>) -> Unit,
) {
    // it's important to use the local instance keeper rather than of the children's for the
    // scopes not to be recreated which is useful in case the exit animation of a config is
    // interrupted by the same config appearing in the stack again while the animation is running
    val stackAnimatorScope = rememberRetained(stackValue.thing.items.first().configuration.hashString() + "stackAnimatorScope") {
        StackAnimatorScope(
            initialStack = stackValue.thing.items.subList(
                if (excludeStartingDestination) 1 else 0,
                stackValue.thing.items.size
            )
        )
    }

    with(stackAnimatorScope) {
        val coroutineScope = rememberCoroutineScope()
        val sourceStack by stackValue.thing.subscribeAsState()

        val holder = rememberSaveableStateHolder()
        holder.retainStates(sourceStack.getConfigurations())

        LaunchedEffect(sourceStack.items, sourceStack.active) {
            onBackstackEmpty(sourceStack.items.size > 1)

            cacheAllChildrenConfigs(
                sourceStack.items.subList(
                    if (excludeStartingDestination) 1 else 0,
                    stackValue.thing.items.size
                )
            )
        }

        Box(modifier) {
            cachedChildren.fastForEach { cachedChild ->
                key(cachedChild.configuration.hashString()) {
                    val inStack = sourceStack.items.fastAny { it.configuration == cachedChild.configuration }
                    val child = remember(inStack) {
                        sourceStack.items.find { it.configuration == cachedChild.configuration } ?: cachedChild
                    }
                    val index = if (inStack) sourceStack.items.indexOf(child) else -1
                    val indexFromTop = if (inStack) sourceStack.items.reversed().indexOf(child) else -1

                    val animData = remember {
                        getOrCreateAnimationData(
                            key = child.configuration,
                            source = animations(child.configuration),
                            initialIndex = index,
                            initialIndexFromTop = indexFromTop
                        )
                    }

                    val allowAnimation by remember {
                        derivedStateOf { indexFromTop <= (animData.renderUntils.min()) }
                    }
                    val animating by remember {
                        derivedStateOf {
                            animData.scopes.any { it.animationStatus.animating }
                        }
                    }

                    LaunchedEffect(animating) {
                        println("animating ${child.configuration} $animating")

                    }

                    val readyForRemoval = !inStack && animData.scopes.fastAll { !it.animationStatus.animating }

                    val render = run {
                        val requireVisibilityInBackstack = animData.requireVisibilityInBackstacks.any { it }

                        val disallowBackstackRender = indexFromTop <= 0 || (allowAnimation && animating)
                        if (requireVisibilityInBackstack) allowAnimation else disallowBackstackRender
                    }

                    // launch animations if there's changes
                    LaunchedEffect(sourceStack.items, indexFromTop) {
                        coroutineScope.launch {
                            animData.scopes.fastForEach { scope ->
                                launch {
                                    scope.updateCurrentIndexAndAnimate(index, indexFromTop, allowAnimation)
                                }
                            }
                        }
                    }

                    LaunchedEffect(readyForRemoval) {
                        if (readyForRemoval) {
                            print(indexFromTop)
                            removeFromCache(child)
                        }
                    }

                    LaunchedEffect(null) {
                        launch {
                            if (allowAnimation) sharedBackEventScope.gestureActions.collectLatest() {
                                // wrapped in run catching because sometimes it can happen
                                // that attempts to update the gesture data in nonexistent scopes
                                // will be made, which will throw an error
                                runCatching { updateGestureDataInScopes(child.configuration, it) }
                            }
                        }
                    }

                    val key = remember { child.configuration.hashString() + " StackAnimator SaveableStateHolder"}
                    Box(
                        Modifier.zIndex((-indexFromTop).toFloat()).accumulate(animData.modifiers),
                        content = { holder.SaveableStateProvider(key) { content(child) }
//                        Text(
//                            animData.scopes.fastMap { it.animationStatus }.joinToString(),
//                            color = Color.White,
//                            modifier = Modifier.padding(
//                                top = (indexFromTop * 10).coerceAtLeast(0).dp
//                            )
//                        )
                        }
                    )
                }
            }
        }
    }
}

@Immutable
private class StackAnimatorScope<C : Any>(initialStack: List<Child.Created<C, DecomposeChildInstance<C>>>) {
    private val animationScopeRegistry = AnimationScopeRegistry()
    private val animationDataRegistry = AnimationDataRegistry<C>(animationScopeRegistry)
    private val mutex = Mutex()
    private var _cachedChildren by mutableStateOf(initialStack)
    val cachedChildren get()= _cachedChildren

    fun getOrCreateAnimationData(key: C, source: ContentAnimations, initialIndex: Int, initialIndexFromTop: Int) =
        animationDataRegistry.getOrCreateAnimationData(key, source, initialIndex, initialIndexFromTop)

    suspend fun cacheAllChildrenConfigs(source: List<Child.Created<C, DecomposeChildInstance<C>>>) {
        val differences = source.filterNot { it in cachedChildren }

        mutex.withLock { _cachedChildren = cachedChildren + differences }
    }

    suspend fun removeFromCache(child: Child.Created<C, DecomposeChildInstance<C>>) {
        mutex.withLock {
            _cachedChildren = cachedChildren - child
            animationDataRegistry.remove(child.configuration)
        }
    }

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
private class AnimationDataRegistry<C : Any> (private val animationScopeRegistry: AnimationScopeRegistry) {
    private val animationData = hashMapOf<String, AnimationData>()

    @OptIn(InternalDecomposeApi::class)
    fun getOrCreateAnimationData(
        key: C,
        source: ContentAnimations,
        initialIndex: Int,
        initialIndexFromTop: Int
    ) = animationData.getOrPut(key.hashString()) {
        val scopes = source.items.fastMap {
            // this will also automatically combine all scopes with the same
            // animation specs, meaning the minimum amount of scopes required
            // will be created
            animationScopeRegistry.getOrPut(it.animationSpec.hashString() + key.hashString() + "animator scope") {
                ContentAnimatorScope(initialIndex, initialIndexFromTop, it.animationSpec)
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
    val scopes = hashMapOf<String, ContentAnimatorScope>()

    inline fun getOrPut(key: String, scope: () -> ContentAnimatorScope) = scopes.getOrPut(key, scope)
    inline fun remove(key: String) {
        val childrensScopes = scopes.filter { it.key.contains(key) }
        childrensScopes.keys.forEach { scopes.remove(it) }
    }
}

@OptIn(InternalDecomposeApi::class)
private fun ChildStack<*, *>.getConfigurations() = items.mapTo(HashSet()) {
    it.configuration.hashString()
}

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
