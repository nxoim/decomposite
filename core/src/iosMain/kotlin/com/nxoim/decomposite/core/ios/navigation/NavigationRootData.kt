package com.nxoim.decomposite.core.ios.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import com.nxoim.decomposite.core.common.navigation.CommonNavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.InternalNavigationRootApi
import com.nxoim.decomposite.core.common.navigation.NavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.cinterop.write
import platform.CoreGraphics.CGFloat
import platform.Foundation.NSCoder
import platform.Foundation.NSError
import platform.SceneKit.SCNScene
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UILocalNotification
import platform.UIKit.UIScreen
import platform.darwin.NSObject
import platform.objc._objc_swiftMetadataInitializer
import kotlin.math.roundToInt

/**
 * iOS specific navigation root provider. Collects the max window size for animations.
 * Uses [CommonNavigationRootProvider].
 *
 * Example:
 * ```kotlin
 * fun MainViewController(navigationRootData: NavigationRootData) = ComposeUIViewController {
 *     NavigationRootProvider(navigationRootData) { App() }
 * }
 * ```
 */
@Suppress("unused") // Used in Swift
@Composable
fun NavigationRootProvider(
    navigationRootData: NavigationRootData,
    content: @Composable () -> Unit
) {
    @OptIn(ExperimentalForeignApi::class)
    val screen by rememberUpdatedState(
        object {
            var width: Int? = null
            var height: Int? = null
        }.apply {
            UIScreen.mainScreen.nativeBounds.useContents {
                width = this.size.width.toInt()
                height = this.size.height.toInt()
            }
        }
    )

    val screenInformation = ScreenInformation(
        widthPx = screen.width
            ?: error("Reported screen width is null. Something wrong happened in Kotlin Swift interop"),
        heightPx = screen.height
            ?: error("Reported screen height is null. Something wrong happened in Kotlin Swift interop"),
        screenShape = ScreenShape(
            path = null,
            corners = null
        )
    )

    @OptIn(InternalNavigationRootApi::class)
    CommonNavigationRootProvider(
        remember { NavigationRoot(screenInformation) },
        navigationRootData,
        content
    )
}

/**
 * iOS specific navigation root provider.  To be used in a root holder on iOS.
 *
 * Example:
 * ```swift
 * class RootHolder: ObservableObject {
 *     let appLifecycle: LifecycleRegistry
 *     private var appStateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)
 *     let navigationRootData: NavigationRootData
 *
 *     init() {
 *         appLifecycle = LifecycleRegistryKt.LifecycleRegistry()
 *
 *         navigationRootData = NavigationRootDataKt.defaultNavigationRootData(
 *             lifecycleRegistry: appLifecycle,
 *             stateKeeper: appStateKeeper
 *         )
 *
 *         LifecycleRegistryExtKt.create(appLifecycle)
 *     }
 *
 *     deinit {
 *         LifecycleRegistryExtKt.destroy(appLifecycle)
 *     }
 *
 *     func saveStateInStateKeeper(coder: NSCoder) {
 *         StateKeeperUtilsKt.save(coder: coder, state: appStateKeeper.save())
 *     }
 * }
 * ```
 */
@Suppress("unused") // Used in Swift
fun defaultNavigationRootData(
    lifecycleRegistry: LifecycleRegistry,
    stateKeeper: StateKeeperDispatcher
) = NavigationRootData(
    defaultComponentContext = DefaultComponentContext(
        lifecycle = lifecycleRegistry,
        stateKeeper = stateKeeper
    )
)