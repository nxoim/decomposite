package com.number869.decomposite.core.common.viewModel

import androidx.compose.runtime.*
import com.arkivanov.essenty.lifecycle.subscribe
import com.number869.decomposite.core.common.ultils.LocalComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

@Stable
@Composable
inline fun <reified T : ViewModel> viewModel(key: String = ""): T {
    val localComponentContext = LocalComponentContext.current
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    val vm = remember(viewModelKey) { viewModelStore.get<T>(viewModelKey) }

    LaunchedEffect(null) {
        localComponentContext.lifecycle.subscribe(
            onDestroy = {
                vm.onDestroy(removeFromViewModelStore = { viewModelStore.remove(viewModelKey) })
            }
        )
    }

    return vm
}

/**
 * Android-like view model instancer. Will get or create a view model instance. Provide
 * [key] if you have multiple instances of the same view model else it will get the first created one.
 *
 * Important: by default gets destroyed based on the local component context's lifecycle.
 * This means the view model will get destroyed on android's configuration changes that
 * destroy the activity or fragment, stopping the view model scope and removing the instance
 * from the view model store. Override [ViewModel.onDestroy] to change this (you can leave it empty).
 */
@Stable
@Composable
inline fun <reified T : ViewModel> viewModel(key: String = "", crossinline viewModel: () -> T): T {
    val localComponentContext = LocalComponentContext.current
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    val vm = remember(viewModelKey) {
        viewModelStore.getOrCreateViewModel(viewModelKey, viewModel)
    }

    LaunchedEffect(null) {
        localComponentContext.lifecycle.subscribe(
            onDestroy = {
                vm.onDestroy(removeFromViewModelStore = { viewModelStore.remove(viewModelKey) })
            }
        )
    }

    return vm
}

/**
 * Basic view model that is similar to the one that's offered by google for android.
 */
@Immutable
open class ViewModel {
    val viewModelScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    open fun onDestroy(removeFromViewModelStore: () -> Unit) {
        viewModelScope.coroutineContext.cancelChildren()
        removeFromViewModelStore()
    }
}