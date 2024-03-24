package com.number869.decomposite.core.common.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.number869.decomposite.core.common.ultils.OnDestinationDisposeEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren


@Stable
@Composable
inline fun <reified T : ViewModel> getExistingViewModel(key: String = ""): T {
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    val vm = remember(viewModelKey) { viewModelStore.get<T>(viewModelKey) }

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
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    val vm = remember(viewModelKey) {
        viewModelStore.getOrCreateViewModel(viewModelKey, viewModel)
    }

    // because of getOrCreate inside OnDestinationDisposeEffect - only the first created
    // instance of it is active
    OnDestinationDisposeEffect(
        T::class.toString() + "ViewModel",
        waitForCompositionRemoval = true
    ) {
        vm.onDestroy(removeFromViewModelStore = { viewModelStore.remove(viewModelKey) })
    }

    return vm
}

@Stable
@Composable
inline fun <reified T : ViewModel> prepareLazyViewModel(key: String = "", noinline viewModel: () -> T) {
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    remember(viewModelKey) { viewModelStore.prepareLazyViewModel(viewModelKey, viewModel) }
}

@Stable
@Composable
inline fun <reified T : ViewModel> getLazyViewModel(key: String = ""): T {
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    val vm = remember(viewModelKey) { viewModelStore.getLazyViewModel<T>(viewModelKey) }

    // because of getOrCreate inside OnDestinationDisposeEffect - only the first created
    // instance of it is active
    OnDestinationDisposeEffect(
        T::class.toString() + "ViewModel",
        waitForCompositionRemoval = true
    ) {
        vm.onDestroy(removeFromViewModelStore = { viewModelStore.remove(viewModelKey) })
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