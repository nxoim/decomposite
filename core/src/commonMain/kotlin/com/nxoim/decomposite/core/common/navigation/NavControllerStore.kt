package com.nxoim.decomposite.core.common.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.reflect.KClass

/**
 * Simple store for navigation controllers.
 */
@Immutable
class NavControllerStore() {
    val store = hashMapOf<Pair<String?, KClass<*>>, Any>()

    inline fun <reified T : Any> get(key: String? = null) = (store[Pair(key, T::class)] as? NavController<T>) ?: error(
        "instance of ${T::class.simpleName} was not found in NavControllerStore"
    )

    inline fun <reified T : Any> getOrCreate(key: String? = null, crossinline creator: () -> NavController<T>) =
        store.getOrPut(Pair(key, T::class)) { creator() } as NavController<T>

    inline fun <reified T> remove(key: String? = null) {
        store[Pair(key, T::class)]?.let { store.remove(Pair(key, T::class)) }
    }
}

val LocalNavControllerStore = staticCompositionLocalOf<NavControllerStore> {
    error("No NavControllerStore provided")
}