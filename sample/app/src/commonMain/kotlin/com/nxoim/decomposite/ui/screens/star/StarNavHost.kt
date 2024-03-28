package com.nxoim.decomposite.ui.screens.star

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.nxoim.decomposite.core.common.navigation.NavHost
import com.nxoim.decomposite.core.common.navigation.animations.cleanSlideAndFade
import com.nxoim.decomposite.core.common.navigation.animations.iosLikeSlide
import com.nxoim.decomposite.core.common.navigation.getExistingNavController
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.ui.screens.star.another.AnotherStarScreen
import com.nxoim.decomposite.ui.screens.star.home.StarHomeScreen

@Composable
fun StarNavHost() {
    val starNavController = navController<StarDestinations>(StarDestinations.Home)

    Scaffold(topBar = { StarTopAppBar() }) { scaffoldPadding ->
        NavHost(
            starNavController,
            Modifier.padding(scaffoldPadding),
            animations = {
                when (currentChild) {
                    StarDestinations.AnotherStar -> iosLikeSlide()
                    else -> cleanSlideAndFade()
                }
            }
        ) { destination ->
            when (destination) {
                StarDestinations.Home -> StarHomeScreen()
                StarDestinations.AnotherStar -> AnotherStarScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarTopAppBar() {
    val navController = getExistingNavController<StarDestinations>()
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