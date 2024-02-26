package com.number869.decomposite.ui.screens.star

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
import com.number869.decomposite.core.common.navigation.animations.animatedDestination
import com.number869.decomposite.core.common.navigation.animations.iosLikeSlide
import com.number869.decomposite.core.common.navigation.navController
import com.number869.decomposite.ui.screens.star.another.AnotherStarScreen
import com.number869.decomposite.ui.screens.star.home.StarHomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarNavHost() {
    NavHost<StarDestinations>(
        startingDestination = StarDestinations.Home,
        routedContent = {
            Scaffold(
                topBar = { StarTopAppBar() },
                content = { scaffoldPadding -> it(Modifier.padding(scaffoldPadding)) }
            )
        }
    ) { destination ->
        when (destination) {
            StarDestinations.Home -> animatedDestination() { StarHomeScreen() }
            StarDestinations.AnotherStar -> animatedDestination(iosLikeSlide()) { AnotherStarScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarTopAppBar() {
    val navController = navController<StarDestinations>()
    val currentScreen by navController.currentScreen.collectAsState()

    TopAppBar(
        title = { Text("Star") },
        navigationIcon = {
            AnimatedVisibility(currentScreen != StarDestinations.Home) {
                IconButton(onClick = { navController.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}