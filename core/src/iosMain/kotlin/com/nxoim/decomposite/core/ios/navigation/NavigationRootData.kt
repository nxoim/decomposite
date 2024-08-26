package com.nxoim.decomposite.core.ios.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
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
 * fun MainViewController(
 *     navigationRootData: NavigationRootData,
 *     screenWidth: CGFloat,
 *     screenHeight: CGFloat,
 * ) = ComposeUIViewController {
 *     NavigationRootProvider(
 *         navigationRootData,
 *         screenWidth,
 *         screenHeight
 *     ) {
 *         App()
 *     }
 * }
 * ```
 */
@NonRestartableComposable
@Composable
fun NavigationRootProvider(
    navigationRootData: NavigationRootData,
    screenWidth: CGFloat,
    screenHeight: CGFloat,
    content: @Composable () -> Unit
) {
    val screenInformation = ScreenInformation(
        widthPx = screenWidth.roundToInt(),
        heightPx = screenHeight.roundToInt(),
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
 *     let navigationRootData: NavigationRootData
 *
 *     init() {
 *         appLifecycle = LifecycleRegistryKt.LifecycleRegistry()
 *
 *         navigationRootData = NavigationRootDataKt.defaultNavigationRootData(lifecycleRegistry: appLifecycle)
 *
 *         LifecycleRegistryExtKt.create(appLifecycle)
 *     }
 *
 *     deinit {
 *         LifecycleRegistryExtKt.destroy(appLifecycle)
 *     }
 * }
 * ```
 */
fun defaultNavigationRootData(
    lifecycleRegistry: LifecycleRegistry
) = NavigationRootData(
    defaultComponentContext = DefaultComponentContext(
        lifecycle = lifecycleRegistry
    )
)