package com.nxoim.decomposite.ui.screens.star

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.nxoim.decomposite.core.common.navigation.animations.iosLikeSlide
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.core.common.ultils.InOverlay
import com.nxoim.decomposite.ui.screens.star.another.AnotherStarScreen
import com.nxoim.decomposite.ui.screens.star.home.StarHomeScreen

@Composable
fun StarNavHost() {
    val starNavController = navController<StarDestinations>(StarDestinations.Home)
    val starOverlayNavController = navController<StarOverlayDestinations>(
        StarOverlayDestinations.Empty
    )

    Scaffold(topBar = { StarTopAppBar(starNavController) }) { scaffoldPadding ->
        NavHost(
            starNavController,
            Modifier.padding(scaffoldPadding)
        ) { destination ->
            when (destination) {
                StarDestinations.Home -> StarHomeScreen(
                    onNavigateToAnotherStar = {
                        starOverlayNavController.navigate(StarOverlayDestinations.AnotherStar)
                    }
                )
            }
        }
    }

    InOverlay {
        NavHost(
            starOverlayNavController,
            excludedDestinations = listOf(StarOverlayDestinations.Empty),
            animations = { iosLikeSlide() }
        ) { destination ->
            when (destination) {
                StarOverlayDestinations.Empty -> {}
                StarOverlayDestinations.AnotherStar -> AnotherStarScreen(starOverlayNavController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarTopAppBar(
    navController: NavController<StarDestinations>
) {
    val currentScreen = navController.currentDestination

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