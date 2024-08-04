package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlin.jvm.JvmInline

/**
 * Kind of like [DisposableEffect], but relies on [InstanceKeeper.Instance]'s onDestroy
 * to make sure an action is done when a component is actually destroyed, surviving
 * configuration changes. Keys must be unique.
 */
@Composable
fun OnDestinationDisposeEffect(
    key: Any,
    waitForCompositionRemoval: Boolean = true,
    componentContext: ComponentContext = LocalComponentContext.current,
    block: @DisallowComposableCalls () -> Unit
) {
    if (waitForCompositionRemoval) {
        DisposableEffect(componentContext, key) {
            componentContext.instanceKeeper.remove(key)
            onDispose { componentContext.onDestroy(key, block) }
        }
    } else {
        remember(componentContext, key) {
            componentContext.instanceKeeper.remove(key)
            componentContext.onDestroy(key, block)
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
) { instanceKeeper.getOrCreate(key) { OnDestroyActionHolder(block) } }

/**
 * Executes [onDispose] when the component is completely destroyed.
 */
@JvmInline
private value class OnDestroyActionHolder(
    val onDispose: @DisallowComposableCalls () -> Unit
) : InstanceKeeper.Instance {
    override fun onDestroy() = onDispose()
}