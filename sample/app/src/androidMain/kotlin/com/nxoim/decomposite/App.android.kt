package com.nxoim.decomposite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.arkivanov.decompose.defaultComponentContext
import com.nxoim.decomposite.core.android.navigation.NavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.ui.theme.SampleTheme

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigationRootData = NavigationRootData(defaultComponentContext())

        setContent {
            enableEdgeToEdge()

            SampleTheme {
                Surface {
                    NavigationRootProvider(navigationRootData) { App() }
                }
            }
        }
    }
}