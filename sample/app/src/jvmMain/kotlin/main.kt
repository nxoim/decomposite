
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.nxoim.decomposite.App
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.navigation.NavigationRootProvider
import com.nxoim.decomposite.ui.theme.SampleTheme
import java.awt.Dimension

@OptIn(ExperimentalDecomposeApi::class)
fun main() = application {
    // initialize these at some root
    val backDispatcher = BackDispatcher()
    val navigationRootData = NavigationRootData(
        DefaultComponentContext(LifecycleRegistry(), backHandler = backDispatcher)
    )

    Window(
        title = "Decomposite",
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)

        SampleTheme {
            // first wrap your app in a theme.
            // because of material 3 quirks - surface wraps the root to fix text
            // colors in overlays.
            Surface {
                // also since you need to initialize the component context of the app
                // on your preferred platform anyway - it's ok to add decomposite to
                // your entry-point/app module of the project, or combine it with your
                // navigation module

                // then initialize the back gesture overlay that will handle the back gestures.
                // initialize it first, put NavigationRoot inside it, else overlays will not
                // detect the gestures
                PredictiveBackGestureOverlay(
                    backDispatcher,
                    backIcon = { _, _ -> }, // no back icon, we handle that on per-screen basis
                    endEdgeEnabled = false, // disable swipes from the right side
                    edgeWidth = (window.size.width / LocalDensity.current.density).dp,
                    content = { NavigationRootProvider(navigationRootData) { App() } }
                )
            }
        }
    }
}