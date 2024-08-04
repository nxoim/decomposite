package com.nxoim.decomposite.core.common.navigation.animations.stack

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Manages the stack cache and helper data for the stack animator.
 */
@Immutable
class StackCacheManager <Key : Any, Instance : Any>(
	private val initialStack: List<Instance>,
	private val itemKey: (Instance) -> Key,
	private val excludedDestinations: (Instance) -> Boolean,
	private val allowBatchRemoval: Boolean
) {
	/**
	 * This is basically a duplicate of the raw source stack. It's necessary
	 * to control the order of operations for correct animation data calculation.
	 */
	var sourceStack by mutableStateOf(initialStack)
		private set

	/**
	 * This is useful for tracking exiting children and their order, which is
	 * not possible with sourceStack.contains(something)
	 */
	val removingChildren = mutableStateListOf<Key>()

	/**
	 * Caching all children in an observable manner, so all updates are
	 * reflected in the ui.
	 */
	val visibleCachedChildren = mutableStateMapOf<Key, Instance>()
		.apply { putAll(sourceStack.fastFilterNot(excludedDestinations).associateBy(itemKey)) }

	fun updateVisibleCachedChildren(newStackRaw: List<Instance>) {
		val newStack = newStackRaw.fastFilterNot(excludedDestinations)
		val oldStack = sourceStack

		val childrenToRemove = oldStack
			.fastFilter { it !in newStack && itemKey(it) !in removingChildren }
		val batchRemoval = childrenToRemove.size > 1 && allowBatchRemoval

		removingChildren.removeAll(newStackRaw.fastMap(itemKey))

		if (batchRemoval) {
			val itemsToRemoveImmediately = childrenToRemove.dropLast(1)

			itemsToRemoveImmediately.fastForEach { visibleCachedChildren.remove(itemKey(it)) }

			removingChildren.add(itemKey(childrenToRemove.last()))
		} else {
			removingChildren.addAll(childrenToRemove.fastMap(itemKey))
		}

		sourceStack = newStackRaw
		visibleCachedChildren.putAll(newStack.associateBy(itemKey))
	}

	fun removeAllRelatedToItem(key: Key) {
		visibleCachedChildren.remove(key)
		removingChildren.remove(key)
	}
}


@Suppress("BanInlineOptIn") // Treat Kotlin Contracts as non-experimental.
@OptIn(ExperimentalContracts::class)
private inline fun <T> List<T>.fastFilterNot(predicate: (T) -> Boolean): List<T> {
	contract { callsInPlace(predicate) }
	val target = ArrayList<T>(size)
	fastForEach { if (!predicate(it)) target += (it) }
	return target
}