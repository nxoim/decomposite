package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastMap
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.number869.decomposite.core.common.ultils.BackGestureEvent
import com.number869.decomposite.core.common.ultils.rememberRetained
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


// it's important to use the local instance keeper rather than of the children's for the
// scopes not to be recreated which is useful in case the exit animation of a config is
// interrupted by the same config appearing in the stack again while the animation is running
@Composable
fun <C : Any> rememberStackAnimatorScope(key: String? = null) = if (key.isNullOrEmpty())
    rememberRetained() { StackAnimatorScope<C>(key) }
else
    rememberRetained("$key StackAnimatorScope") { StackAnimatorScope<C>(key) }

@Immutable
class StackAnimatorScope<C : Any>(val key: String?) {
    val animationDataRegistry = AnimationDataRegistry<C>()
    val childAnimPrerequisites = hashMapOf<C, ChildAnimPrerequisites>()

    internal fun getOrCreateAnimationData(key: C, source: ContentAnimations, initialIndex: Int, initialIndexFromTop: Int) =
        animationDataRegistry.getOrCreateAnimationData(key, source, initialIndex, initialIndexFromTop)

    internal fun removeFromCache(target: C) { animationDataRegistry.remove(target) }

    suspend inline fun updateGestureDataInScopes(backGestureData: BackGestureEvent) = coroutineScope {
        animationDataRegistry.forEach { (configuration, animationData) ->
            val prerequisites = childAnimPrerequisites[configuration] ?: ChildAnimPrerequisites(
                allowAnimation = false,
                inStack = false
            )

            if (prerequisites.inStack && prerequisites.allowAnimation) {
                animationData.scopes.forEach { (_, scope) ->
                    launch { scope.onBackGesture(backGestureData) }
                }
            }
        }
    }

    inline fun Modifier.accumulate(modifiers: List<Modifier>) = modifiers.fold(this) { acc, modifier ->
        acc.then(modifier)
    }

    fun updateChildAnimPrerequisites(configuration: C, allowAnimation: Boolean, inStack: Boolean) {
        childAnimPrerequisites[configuration] = ChildAnimPrerequisites(allowAnimation, inStack)
    }
}

data class AnimationData(
    val scopes: Map<String, ContentAnimatorScope>,
    val modifiers: List<Modifier>,
    val renderUntils: List<Int>,
    val requireVisibilityInBackstacks: List<Boolean>,
)

data class ChildAnimPrerequisites(
    val allowAnimation: Boolean,
    val inStack: Boolean
)
class AnimationDataRegistry<C : Any> () {
    private val animationScopeRegistry = AnimationScopeRegistry()
    private val animationData = hashMapOf<C, AnimationData>()

    fun getOrCreateAnimationData(
        key: C,
        source: ContentAnimations,
        initialIndex: Int,
        initialIndexFromTop: Int
    ) = animationData[key] ?: animationData.getOrPut(key) {
        val scopes = source.items.fastMap {
            // this will also automatically combine all scopes with the same
            // animation specs, meaning the minimum amount of scopes required
            // will be created
            it.key to animationScopeRegistry.getOrPut(it.key + it::class.qualifiedName + key + "animator scope") {
                it.animatorScopeFactory(initialIndex, initialIndexFromTop)
            }
        }.toMap()

        AnimationData(
            scopes = scopes,
            modifiers = source.items.fastMap {
                (it.animationModifier as ContentAnimatorScope.() -> Modifier).invoke((scopes[it.key]!!))
            },
            renderUntils = source.items.fastMap { it.renderUntil },
            requireVisibilityInBackstacks = source.items.fastMap { it.requireVisibilityInBackstack },
        )
    }

    fun get(key: C) = animationData[key] ?: error("No animation data for $key in AnimationDataRegistry")

    fun forEach(item: (Map.Entry<C, AnimationData>) -> Unit) {
        animationData.forEach { item(it) }
    }

    @OptIn(InternalDecomposeApi::class)
    fun remove(key: C) {
        animationData.remove(key)
        animationScopeRegistry.remove(key.hashString())
    }
}

class AnimationScopeRegistry {
    val scopes = hashMapOf<String, ContentAnimatorScope>()

    inline fun getOrPut(key: String, scope: () -> ContentAnimatorScope) = scopes.getOrPut(key, scope)
    fun remove(key: String) {
        val childrensScopes = scopes.filter { it.key.contains(key) }
        childrensScopes.keys.forEach { scopes.remove(it) }
    }
}