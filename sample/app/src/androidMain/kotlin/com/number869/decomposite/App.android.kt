package com.number869.decomposite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.number869.decomposite.core.common.navigation.navigationRootDataProvider
import org.koin.dsl.module

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // koin koin koin of koin koin
        KoinApp.koinInstance.koin.loadModules(
            listOf(
                module { single { navigationRootDataProvider(defaultComponentContext()) } }
            )
        )

        setContent {
            enableEdgeToEdge()

            App()
        }
    }
}