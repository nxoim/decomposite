package com.nxoim.decomposite.core.android.navigation

import android.app.Activity
import android.os.Build
import android.view.RoundedCorner
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.retainedComponent
import com.nxoim.decomposite.core.common.navigation.CommonNavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.InternalNavigationRootApi
import com.nxoim.decomposite.core.common.navigation.NavControllerStore
import com.nxoim.decomposite.core.common.navigation.NavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import com.nxoim.decomposite.core.common.ultils.ScreenShape
import com.nxoim.decomposite.core.common.ultils.ScreenShapeCorners
import com.nxoim.decomposite.core.common.viewModel.ViewModelStore

/**
 * Android specific [NavigationRoot] and [NavigationRootData] provider.
 * Collects the screen size and shape for animations.
 */
@Composable
fun NavigationRootProvider(navigationRootData: NavigationRootData, content: @Composable () -> Unit) {
    val windowManager = (LocalView.current.context as Activity).window.context
        .getSystemService(WindowManager::class.java) as WindowManager

    val insets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        windowManager.maximumWindowMetrics.windowInsets
    else
        null

    val screenCorners = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        runCatching {
            insets?.let {
                ScreenShapeCorners(
                    insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius!!,
                    insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)?.radius!!,
                    insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)?.radius!!,
                    insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)?.radius!!
                )
            }
        }.getOrNull()
    else
        null

    val screenSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        windowManager.maximumWindowMetrics.bounds.let { IntSize(it.width(), it.height()) }
    else
        LocalContext.current.resources.displayMetrics.let { IntSize(it.widthPixels, it.heightPixels) }

    val screenInformation = ScreenInformation(
        widthPx = screenSize.width,
        heightPx = screenSize.height,
        screenShape = ScreenShape(
            path = null,
            corners = screenCorners
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
 * Creates an Android specific root of the app for back gesture handling,
 * storing view models, and navigation controller instances. **[ComponentContext]
 * is retained using [retainedComponent]**.
 *
 * [NavigationRootData] is responsible for setting up the root context for navigation and
 * state management within the application. It provides access to essential
 * components like the [ViewModelStore], [NavControllerStore], and the default
 * [ComponentContext].
 *
 * [ViewModelStore] by default is wrapped into the default component context's instance keeper.
 *
 * Initialize this outside of setContent.
 *
 * Example:
 * ```kotlin
 * class AppActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         val navigationRootData = defaultNavigationRootData()
 *
 *         setContent {
 *             enableEdgeToEdge()
 *
 *             SampleTheme {
 *                 Surface {
 *                     NavigationRootProvider(navigationRootData) { App() }
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */
fun ComponentActivity.defaultNavigationRootData() = retainedComponent {
    NavigationRootData(
        DefaultComponentContext(
            it.lifecycle,
            it.stateKeeper,
            it.instanceKeeper,
            it.backHandler
        )
    )
}