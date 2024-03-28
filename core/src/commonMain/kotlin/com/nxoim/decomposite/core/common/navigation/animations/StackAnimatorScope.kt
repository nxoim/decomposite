package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import com.nxoim.decomposite.core.common.ultils.rememberRetained
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

    internal fun removeAnimationDataFromCache(target: C) {
        animationDataRegistry.remove(target)
        childAnimPrerequisites.remove(target)
    }

    internal fun removeStaleAnimationDataCache(nonStale: List<C>) {
        val stale = childAnimPrerequisites.filter { it.key !in nonStale }.map { it.key }
        stale.forEach(::removeAnimationDataFromCache)
    }

    suspend inline fun updateGestureDataInScopes(backGestureData: BackGestureEvent) = coroutineScope {
        kotlin.runCatching {
            animationDataRegistry.forEach { (configuration, animationData) ->
                val prerequisites = childAnimPrerequisites[configuration] ?: ChildAnimPrerequisites(
                    allowAnimation = false,
                    inStack = false
                )

                if (prerequisites.inStack && prerequisites.allowAnimation) {
                    animationData.scopes.forEach { (_, scope) ->
                        launch {  scope.onBackGesture(backGestureData) }
                    }
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

@Immutable
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

class AnimationDataRegistry<C : Any> {
    private val animationData = hashMapOf<C, AnimationData>()
    private val scopeRegistry = hashMapOf<Pair<C, String>, ContentAnimatorScope>()

    fun getOrCreateAnimationData(
        key: C,
        source: ContentAnimations,
        initialIndex: Int,
        initialIndexFromTop: Int
    ): AnimationData {
        // check if we already have existing data for this key
        val existingData = animationData[key]
        if (existingData != null) {
            // if we have existing data, update it
            val updatedScopes = mutableMapOf<String, ContentAnimatorScope>()
            val updatedModifiers = mutableListOf<Modifier>()
            val updatedRenderUntils = mutableListOf<Int>()
            val updatedRequireVisibilityInBackstacks = mutableListOf<Boolean>()

            source.items.forEach { animator ->
                // get existing or create the scope for this animator
                val scopeKey = Pair(key, animator.key)
                val scope = scopeRegistry.getOrPut(scopeKey) {
                    animator.animatorScopeFactory(initialIndex, initialIndexFromTop)
                }
                updatedScopes[animator.key] = scope
                updatedModifiers.add((animator.animationModifier as ContentAnimatorScope.() -> Modifier).invoke(scope))
                updatedRenderUntils.add(animator.renderUntil)
                updatedRequireVisibilityInBackstacks.add(animator.requireVisibilityInBackstack)
            }

            val newData = AnimationData(
                updatedScopes,
                updatedModifiers,
                updatedRenderUntils,
                updatedRequireVisibilityInBackstacks
            )
            animationData[key] = newData

            return newData
        } else {
            // if we don't have existing data, create a new one
            val scopes = mutableMapOf<String, ContentAnimatorScope>()
            val modifiers = mutableListOf<Modifier>()
            val renderUntils = mutableListOf<Int>()
            val requireVisibilityInBackstacks = mutableListOf<Boolean>()

            source.items.forEach { animator ->
                val scopeKey = Pair(key, animator.key)
                val scope = scopeRegistry.getOrPut(scopeKey) {
                    animator.animatorScopeFactory(initialIndex, initialIndexFromTop)
                }
                scopes[animator.key] = scope
                @Suppress("UNCHECKED_CAST") // because it's never Nothing.()
                modifiers.add((animator.animationModifier as ContentAnimatorScope.() -> Modifier).invoke(scope))
                renderUntils.add(animator.renderUntil)
                requireVisibilityInBackstacks.add(animator.requireVisibilityInBackstack)
            }

            val newData = AnimationData(scopes, modifiers, renderUntils, requireVisibilityInBackstacks)
            animationData[key] = newData
            return newData
        }
    }

    fun get(key: C) = animationData[key]
        ?: error("No animation data for $key in AnimationDataRegistry")

    fun forEach(item: (Map.Entry<C, AnimationData>) -> Unit) = animationData.forEach { item(it) }

    fun remove(key: C) {
        animationData.remove(key)
        scopeRegistry.keys.removeAll { it.first == key }
    }
}