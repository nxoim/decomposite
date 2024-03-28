package com.nxoim.decomposite.core.common.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
class ViewModelStore {
    val lazyVmReferences = hashMapOf<Any, Any>()
    val store = hashMapOf<Any, Any>()

    inline fun <reified T : ViewModel> get(key: Any) = (store[key] as? T) ?: error(
        "instance of ${T::class.simpleName} was not found in ViewModelStore"
    )

    inline fun <reified T : ViewModel> getOrCreateViewModel(key: Any, crossinline creator: () -> T): T {
        return store.getOrPut(key) { creator() } as T
    }

    inline fun <reified T : ViewModel> prepareLazyViewModel(key: Any, noinline creator: () -> T) {
        lazyVmReferences[key] = creator
    }

    inline fun <reified T : ViewModel> getLazyViewModel(key: Any, ): T {
        val vm = store.getOrPut(key) {
            val viewModelFactoryLambda = (lazyVmReferences[key] as? () -> T) ?: error(
                "Attempted lazy retrieval of a ${T::class.simpleName} instance, which isn't lazy"
            )

            viewModelFactoryLambda.invoke()
        }

        return vm as T
    }

    fun remove(key: String) { store[key]?.let { store.remove(key) } }
}

@Stable
val LocalViewModelStore = staticCompositionLocalOf<ViewModelStore> {
    error("No ViewModelStore provided")
}