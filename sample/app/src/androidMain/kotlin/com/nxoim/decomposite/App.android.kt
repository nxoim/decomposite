package com.nxoim.decomposite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import com.nxoim.decomposite.core.android.navigation.NavigationRootProvider
import com.nxoim.decomposite.core.android.utils.backGestureDispatcher
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.navigation.PredictiveBackGestureOverlay
import com.nxoim.decomposite.ui.theme.SampleTheme

class AppActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = defaultComponentContext()
        val backDispatcher = backGestureDispatcher()
        val navigationRootData = NavigationRootData(
            DefaultComponentContext(
                context.lifecycle,
                context.stateKeeper,
                context.instanceKeeper,
                backDispatcher
            )
        )

        setContent {
            enableEdgeToEdge()

            SampleTheme {
                Surface {
                    PredictiveBackGestureOverlay(
                        backDispatcher,
                        backIcon = { _, _ -> }, // no back icon, we handle that on per-screen basis
                        endEdgeEnabled = false, // disable swipes from the right side,
                        edgeWidth = 999.dp,
                        content = { NavigationRootProvider(navigationRootData) { App() } }
                    )
                }
            }
        }
    }
}