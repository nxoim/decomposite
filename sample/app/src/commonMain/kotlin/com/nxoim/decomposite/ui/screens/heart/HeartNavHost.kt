package com.nxoim.decomposite.ui.screens.heart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nxoim.decomposite.core.common.navigation.NavHost
import com.nxoim.decomposite.core.common.navigation.animations.cleanSlideAndFade
import com.nxoim.decomposite.core.common.navigation.animations.iosLikeSlide
import com.nxoim.decomposite.core.common.navigation.getExistingNavController
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.ui.screens.heart.another.AnotherHeartScreen
import com.nxoim.decomposite.ui.screens.heart.home.HeartHomeScreen

@Composable
fun HeartNavHost() {
    val heartNavController = navController<HeartDestinations>(HeartDestinations.Home)

    Scaffold(topBar = { HeartTopAppBar() }) { scaffoldPadding ->
        NavHost<HeartDestinations>(
            heartNavController,
            Modifier.padding(scaffoldPadding),
            animations = {
                when (currentChild) {
                    is HeartDestinations.AnotherHeart -> iosLikeSlide()
                    else -> cleanSlideAndFade()
                }
            }
        ) { destination ->
            when (destination) {
                HeartDestinations.Home -> HeartHomeScreen()

                is HeartDestinations.AnotherHeart -> AnotherHeartScreen(destination.text)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeartTopAppBar() {
    val navController = getExistingNavController<HeartDestinations>()
    val currentScreen =  navController.currentScreen

    TopAppBar(
        title = { Text("Heart") },
        navigationIcon = {
            AnimatedVisibility(currentScreen != HeartDestinations.Home) {
                IconButton(onClick = navController::navigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            AnimatedVisibility(currentScreen != HeartDestinations.Home) {
                IconButton(onClick = { navController.replaceAll(HeartDestinations.Home)}) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }
    )
}