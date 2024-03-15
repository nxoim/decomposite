package com.number869.decomposite.core.common.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
class NavControllerStore() {
    val store = hashMapOf<String, Any>()

    inline fun <reified T : Any> get() = (store[T::class.toString()] as? NavController<T>) ?: error(
        "instance of ${T::class.simpleName} was not found in NavControllerStore"
    )

    inline fun <reified T : Any> getOrCreate(crossinline creator: () -> NavController<T>) =
        store.getOrPut(T::class.toString()) { creator() } as NavController<T>

    inline fun <reified T> remove() { store[T::class.toString()]?.let { store.remove(T::class.toString()) } }
}

val LocalNavControllerStore = staticCompositionLocalOf<NavControllerStore> {
    error("No NavControllerStore provided")
}