package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate

/**
 * Kind of like [DisposableEffect], but relies on [InstanceKeeper.Instance]'s onDestroy
 * to make sure an action is done when a component is actually destroyed, surviving
 * configuration changes. Keys must be unique.
 */
@Composable
fun OnDestinationDisposeEffect(
    key: Any? = null,
    waitForCompositionRemoval: Boolean = true,
    componentContext: ComponentContext = LocalComponentContext.current,
    block: @DisallowComposableCalls () -> Unit
) = key(componentContext, waitForCompositionRemoval) {
    val keeper = componentContext.instanceKeeper
    val finalKey = currentCompositeKeyHash.toString(36) + key.hashCode()

    if (waitForCompositionRemoval) DisposableEffect(Unit) {
        onDispose { componentContext.onDestroy(finalKey, block) }
    } else {
        var holder by remember {
            mutableStateOf(
                keeper.getOrCreate(finalKey) { OnDestroyActionHolder(finalKey, block) }
            )
        }

        SideEffect {
            if (holder.key != finalKey) {
                keeper.remove(holder.key)
                holder = keeper.getOrCreate(finalKey) { OnDestroyActionHolder(finalKey, block) }
            }
        }
    }
}

/**
 * Saves the call in a container that executes the call before getting fully destroyed,
 * surviving configuration changes, making sure the block is only executed when
 * a component fully dies,
 */
fun ComponentContext.onDestroy(
    key: Any,
    block: @DisallowComposableCalls () -> Unit
) { instanceKeeper.getOrCreate(key) { OnDestroyActionHolder(key, block) } }

/**
 * Executes [onDispose] when the component is completely destroyed.
 */
private class OnDestroyActionHolder(
    val key: Any,
    val onDispose: @DisallowComposableCalls () -> Unit
) : InstanceKeeper.Instance {
    override fun onDestroy() = onDispose()
}