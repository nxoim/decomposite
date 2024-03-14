package com.number869.decomposite.core.common.ultils

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
inline fun OnDestinationDisposeEffect(
    key: Any,
    componentContext: ComponentContext = LocalComponentContext.current,
    crossinline block: @DisallowComposableCalls () -> Unit
) {
    DisposableEffect(null) {
        onDispose { componentContext.instanceKeeper.remove(key) }
    }

    remember { componentContext.onDestroyDisposableEffect(key, block) }
}

inline fun ComponentContext.onDestroyDisposableEffect(
    key: Any,
    crossinline block: @DisallowComposableCalls () -> Unit
) = instanceKeeper.getOrCreate(key) { OnDestroyActionHolder { block() } }

@JvmInline
value class OnDestroyActionHolder(val onDispose: () -> Unit) : InstanceKeeper.Instance {
    override fun onDestroy() = onDispose()
}