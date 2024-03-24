package com.number869.decomposite.core.common.navigation.snacks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.number869.decomposite.core.common.navigation.DefaultChildInstance
import com.number869.decomposite.core.common.navigation.LocalNavigationRoot
import com.number869.decomposite.core.common.navigation.animations.ContentAnimations
import com.number869.decomposite.core.common.navigation.animations.fade
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.serializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Gets an existing navigation controller instance.
 */
@ReadOnlyComposable
@Composable
inline fun snackController() = LocalNavigationRoot.current.snackController

@Immutable
class SnackController(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val scope = MainScope()
    private val mutex = Mutex()

    val contentOfSnacks = instanceKeeper.getOrCreate("contentOfSnacks") {
        SnacksContentHolder(mutableStateMapOf<String, @Composable () -> Unit>())
    }.data

    val animationsForDestinations = instanceKeeper.getOrCreate("animationsForSnacks") {
        AnimationsHolder(mutableMapOf<String, ContentAnimations>())
    }.data

    private val snackNavigation = StackNavigation<String>()

    val snackStack = childStack(
        source = snackNavigation,
        serializer = String.serializer(),
        initialConfiguration = "empty",
        key = "snackStack",
        handleBackButton = false,
        childFactory = { _, context -> DefaultChildInstance(context) }
    )

    @OptIn(ExperimentalDecomposeApi::class)
    fun display(
        key: String,
        animation: () -> ContentAnimations = { fade() },
        displayDurationMillis: Duration = 5.seconds,
        content: @Composable BoxScope.() -> Unit
    ) = scope.launch {
        mutex.withLock {
            // remember data about content. the content is removed from within
            // the nav host using DisposableEffect for proper animations using
            animationsForDestinations[key] = animation()
            contentOfSnacks[key] = { Box(content = content, modifier = Modifier.fillMaxSize()) }

            snackNavigation.pushToFront(key)
            delay(displayDurationMillis)
            hide(key)

            // delay before displaying another snack
            delay(150)
        }
    }

    fun removeSnackContents(key: String) {
        contentOfSnacks.remove(key)
        animationsForDestinations.remove(key)
    }

    fun hide(key: String, onComplete: () -> Unit = { }) {
        val stackWithoutThisKeyAsArrayOfKeys = snackStack.backStack
            .filterNot { it.configuration == key }
            .map { it.configuration }
            .toTypedArray()

        snackNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys) { onComplete() }
    }

    fun cleanQueue() {
        snackNavigation.replaceAll("empty")
        mutex.unlock()
        scope.coroutineContext.cancelChildren()
    }
}

@Immutable
private data class AnimationsHolder<T>(val data: T) : InstanceKeeper.Instance
@Immutable
private data class SnacksContentHolder<T>(val data: T) : InstanceKeeper.Instance