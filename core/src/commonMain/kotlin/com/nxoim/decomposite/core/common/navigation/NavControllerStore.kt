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

    /**
     * DO NOT CALL DIRECTLY. Use [navController]
     * @see navController
     */
    fun <T : Any> get(key: String? = null, kClass: KClass<T>) =
        (store[Pair(key, kClass)] as NavController<T>?)

    /**
     * DO NOT CALL DIRECTLY. Use [navController]
     * @see navController
     */
    fun <T : Any> getOrCreate(
        key: String? = null,
        kClass: KClass<T>,
        creator: () -> NavController<T>
    ) = store.getOrPut(Pair(key, kClass)) { creator() } as NavController<T>

    /**
     * DO NOT CALL DIRECTLY. Use [navController]
     * @see navController
     */
    fun <T : Any> remove(key: String? = null, kClass: KClass<T>) {
        store[Pair(key, kClass)]?.let { store.remove(Pair(key, kClass)) }
    }
}

val LocalNavControllerStore = staticCompositionLocalOf<NavControllerStore> {
    error("No NavControllerStore provided")
}