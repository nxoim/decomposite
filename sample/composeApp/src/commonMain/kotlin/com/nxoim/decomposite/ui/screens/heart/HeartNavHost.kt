package com.nxoim.decomposite.ui.screens.heart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nxoim.decomposite.core.common.navigation.NavController
import com.nxoim.decomposite.core.common.navigation.NavHost
import com.nxoim.decomposite.core.common.navigation.animations.cleanSlideAndFade
import com.nxoim.decomposite.core.common.navigation.animations.iosLikeSlide
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.ui.screens.heart.another.AnotherHeartScreen
import com.nxoim.decomposite.ui.screens.heart.home.HeartHomeScreen

@Composable
fun HeartNavHost() {
    val heartNavController = navController<HeartDestinations>(HeartDestinations.Home)

    Scaffold(topBar = { HeartTopAppBar(heartNavController) }, bottomBar = { Spacer(Modifier) }) { scaffoldPadding ->
        NavHost(
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
                HeartDestinations.Home -> HeartHomeScreen(heartNavController)

                is HeartDestinations.AnotherHeart -> AnotherHeartScreen(destination.text, heartNavController)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeartTopAppBar(
    navController: NavController<HeartDestinations>
) {
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