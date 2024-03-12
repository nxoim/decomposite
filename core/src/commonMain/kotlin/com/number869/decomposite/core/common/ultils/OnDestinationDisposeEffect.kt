package com.number869.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlin.jvm.JvmInline

/**
 * Kind of like [DisposableEffect], but relies on [InstanceKeeper.Instance]'s onDestroy
 * to make sure an action is done when a component is actually destroyed, surviving
 * configuration changes. Keys must be unique. Will throw an error if more than 1 instance
 * of this function has same keys.
 */
@Composable
inline fun OnDestinationDisposeEffect(
    key: Any,
    crossinline block: @DisallowComposableCalls () -> Unit
) {
    val componentContext = LocalComponentContext.current

    DisposableEffect(null) {
        onDispose { componentContext.instanceKeeper.remove(key) }
    }

    remember { componentContext.onDestroyDisposableEffect(key, block) }
}

inline fun ComponentContext.onDestroyDisposableEffect(
    key: Any,
    crossinline block: @DisallowComposableCalls () -> Unit
) = instanceKeeper.put(key, OnDestroyActionHolder { block() })

@JvmInline
value class OnDestroyActionHolder(val onDispose: () -> Unit) : InstanceKeeper.Instance {
    override fun onDestroy() = onDispose()
}