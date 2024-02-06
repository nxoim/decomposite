package com.number869.decomposite.core.common.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
class ViewModelStore {
    val store = hashMapOf<Any, Any>()

    inline fun <reified T : ViewModel> getOrCreateViewModel(key: Any, crossinline creator: () -> T): T {
        return store.getOrPut(key) { creator() } as T
    }

    fun remove(key: String) { store[key]?.let { store.remove(key) } }
}

@Stable
val LocalViewModelStore = staticCompositionLocalOf<ViewModelStore> {
    error("No DecomposeViewModelStore provided")
}