package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.nxoim.decomposite.core.common.navigation.NavHost
import kotlin.jvm.JvmInline


/**
 * Alternative to [rememberSaveable] that keeps the value alive during configuration
 * changes, but not process death. Remembers and stores the value in
 * the [InstanceKeeper] tied to [ComponentContext] provided by [LocalComponentContext],
 * meaning the value will be retained as long as the component/backstack entry (provided
 * by [NavHost]) exists.
 */
@Stable
@Composable
fun <T : Any> rememberRetained(
	key: Any? = null,
	componentContext: ComponentContext = LocalComponentContext.current,
	block: @DisallowComposableCalls () -> T
): T = key(componentContext) {
	val keeper = componentContext.instanceKeeper
	val finalKey = currentCompositeKeyHash.toString(36) + key.hashCode()

	var instance by remember {
		mutableStateOf(componentContext.instantiateValue(finalKey, block))
	}

	SideEffect {
		if (instance.key != finalKey) {
			keeper.remove(instance.key)
			instance = componentContext.instantiateValue(finalKey, block)
		}
	}

	return@key instance.content
}

private inline fun <T : Any> ComponentContext.getRetainedValue(finalKey: Any) =
	(instanceKeeper.get(finalKey) as ValueInstance<T>?)?.instance

private inline fun <T : Any> ComponentContext.instantiateValue(
	finalKey: Any,
	default: () -> T
) = RetainedItem(finalKey, getRetainedValue(finalKey) ?: default())
	.also {
		instanceKeeper.remove(finalKey) // make sure data is updated
		instanceKeeper.put(finalKey, ValueInstance(it.content))
	}

@JvmInline
private value class ValueInstance<T : Any>(val instance: T) : InstanceKeeper.Instance

private data class RetainedItem<T : Any>(val key: Any, val content: T)