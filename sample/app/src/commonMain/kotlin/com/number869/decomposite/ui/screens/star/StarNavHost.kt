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
import com.number869.decomposite.ui.screens.star.another.AnotherStarScreen
import com.number869.decomposite.ui.screens.star.home.StarHomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarNavHost() {
    NavHost<StarDestinations>(
        startingDestination = StarDestinations.Home,
        routedContent = {
            val currentScreen by currentScreen.collectAsState()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Star") },
                        navigationIcon = {
                            AnimatedVisibility(currentScreen != StarDestinations.Home) {
                                IconButton(onClick = { navigateBack() }) {
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
            StarDestinations.Home -> StarHomeScreen()
            StarDestinations.AnotherStar -> AnotherStarScreen()
        }
    }
}