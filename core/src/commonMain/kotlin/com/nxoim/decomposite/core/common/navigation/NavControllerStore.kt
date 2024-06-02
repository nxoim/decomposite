package com.nxoim.decomposite.core.common.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.reflect.KClass

/**
 * Simple store for navigation controllers.
 */
@Immutable
class NavControllerStore() {
    private val store = hashMapOf<Pair<String?, KClass<*>>, Any>()

    fun <T : Any> get(key: String? = null, kClass: KClass<T>) =
        (store[Pair(key, kClass)] as? NavController<T>)
            ?: error("instance of ${kClass.simpleName} was not found in NavControllerStore")

    fun <T : Any> getOrCreate(
        key: String? = null,
        kClass: KClass<T>,
        creator: () -> NavController<T>
    ) = store.getOrPut(Pair(key, kClass)) { creator() } as NavController<T>

    fun <T : Any> remove(key: String? = null, kClass: KClass<T>) {
        store[Pair(key, kClass)]?.let { store.remove(Pair(key, kClass)) }
    }
}

val LocalNavControllerStore = staticCompositionLocalOf<NavControllerStore> {
    error("No NavControllerStore provided")
}