package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
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
inline fun OnDestinationDisposeEffect(
    key: Any,
    waitForCompositionRemoval: Boolean = false,
    componentContext: ComponentContext = LocalComponentContext.current,
    crossinline block: @DisallowComposableCalls () -> Unit
) {
    if (waitForCompositionRemoval) {
        val componentDestructionDetector = remember(componentContext) {
            componentContext.instanceKeeper.getOrCreate(key) {
                ComponentDestructionDetector()
            }
        }
        // this works because the component gets destroyed first, triggering
        // onDestroy, then the composable
        DisposableEffect(true) {
            onDispose { if (componentDestructionDetector.destroyed) block() }
        }
    } else {
        remember(componentContext) {
            componentContext.onDestroyDisposableEffect(key, block)
        }
    }
}

/**
 * Saves the call in a container that executes the call before getting fully destroyed,
 * surviving configuration changes, making sure this is only executed when
 * a component fully dies,
 */
inline fun ComponentContext.onDestroyDisposableEffect(
    key: Any,
    crossinline block: @DisallowComposableCalls () -> Unit
) = instanceKeeper.getOrCreate(key) { OnDestroyActionHolder { block() } }

/**
 * Executes [onDispose] when the component is completely destroyed.
 */
@JvmInline
value class OnDestroyActionHolder(val onDispose: () -> Unit) : InstanceKeeper.Instance {
    override fun onDestroy() = onDispose()
}

@Immutable
class ComponentDestructionDetector() : InstanceKeeper.Instance {
    var destroyed = false
    override fun onDestroy() { destroyed = true }
}