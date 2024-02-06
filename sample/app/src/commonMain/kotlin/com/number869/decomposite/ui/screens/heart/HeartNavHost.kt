package com.number869.decomposite.ui.screens.heart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.number869.decomposite.core.common.navigation.NavHost
import com.number869.decomposite.core.common.navigation.navController
import com.number869.decomposite.ui.screens.heart.another.AnotherHeartScreen
import com.number869.decomposite.ui.screens.heart.home.HeartHomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartNavHost() {
    NavHost<HeartDestinations>(
        startingDestination = HeartDestinations.Home,
        routedContent = {
            // this doesn't throw an error because it's initialized before contained
            // content executes
            val navController = navController<HeartDestinations>()
            val currentScreen by navController.currentScreen.collectAsState()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Heart") },
                        navigationIcon = {
                            AnimatedVisibility(currentScreen != HeartDestinations.Home) {
                                IconButton(onClick = { navController.navigateBack() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        }
                    )
                }
            ) { scaffoldPadding ->
                it(Modifier.padding(scaffoldPadding))
            }
        }
    ) { destination ->
        when (destination) {
            HeartDestinations.Home -> HeartHomeScreen()
            is HeartDestinations.AnotherHeart -> AnotherHeartScreen(destination.text)
        }
    }
}