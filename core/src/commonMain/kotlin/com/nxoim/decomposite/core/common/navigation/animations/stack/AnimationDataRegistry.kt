package com.nxoim.decomposite.core.common.navigation.animations.stack

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapNotNull
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.scopes.ContentAnimatorScope

class AnimationDataRegistry<Key : Any> {
	private val animationDataCache = hashMapOf<Key, AnimationData>()
	private val scopeRegistry = mutableStateMapOf<Pair<Key, String>, ContentAnimatorScope>()

	fun getOrCreateAnimationData(
		key: Key,
		source: ContentAnimations,
		initialIndex: Int,
		initialIndexFromTop: Int
	): AnimationData {
		source.items.fastForEach { animator ->
			val scopeKey = Pair(key, animator.key)
			scopeRegistry.getOrPut(scopeKey) {
				animator.animatorScopeFactory(initialIndex, initialIndexFromTop)
			}
		}

		val scopesFromRegistry = scopeRegistry
			.filterKeys { it.first == key }
			.map { it.key.second to it.value }
			.toMap()

		return AnimationData(
			scopes = { scopesFromRegistry },
			modifiers = {
				source.items.fastMapNotNull {
					scopesFromRegistry[it.key]?.let { scope -> it.animationModifier(scope) }
				}
			},
			renderUntils = { source.items.fastMap { it.renderUntil } },
			requireVisibilityInBackstacks = {
				source.items.fastMap { it.requireVisibilityInBackstack }
			}
		).also { animationDataCache[key] = it }
	}

	fun forEach(block: (Map.Entry<Key, AnimationData>) -> Unit) {
		animationDataCache.forEach(block)
	}

	fun remove(key: Key) {
		scopeRegistry.keys.removeAll { it.first == key }
		animationDataCache.remove(key)
	}
}