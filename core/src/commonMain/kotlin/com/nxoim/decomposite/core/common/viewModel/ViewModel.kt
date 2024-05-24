package com.nxoim.decomposite.core.common.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.nxoim.decomposite.core.common.ultils.OnDestinationDisposeEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

/**
 * Gets an existing view model instance. Does not manage the view model's lifecycle.
 */
@Stable
@Composable
inline fun <reified T : ViewModel> getExistingViewModel(key: String = ""): T {
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    val vm = remember(viewModelKey) { viewModelStore.get<T>(viewModelKey) }

    return vm
}

/**
 * Android-like view model instancer. Will get or create a [ViewModel] instance in
 * the [LocalViewModelStore]. Provide [key] if you have multiple instances of
 * the same view model, or it will get the first created one. [ViewModel.onDestroy] method
 * will be called when the component/destination is removed and AFTER the composable gets destroyed.
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
        viewModelKey + "ViewModel",
        waitForCompositionRemoval = true
    ) {
        vm.onDestroy(removeFromViewModelStore = { viewModelStore.remove(viewModelKey) })
    }

    return vm
}

/**
 * Prepares a simple lazy view model. A reference is saved in [ViewModelStore] which is
 * later called by [getLazyViewModel]
 */
@Stable
@Composable
inline fun <reified T : ViewModel> prepareLazyViewModel(key: String = "", noinline viewModel: () -> T) {
    val viewModelStore = LocalViewModelStore.current
    val viewModelKey = key + T::class.toString()
    remember(viewModelKey) { viewModelStore.prepareLazyViewModel(viewModelKey, viewModel) }
}

/**
 * Creates a view model instance using the prepared (by [prepareLazyViewModel]) reference
 * if an instance does not exist. The first instance manages the view model's lifecycle.
 */
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