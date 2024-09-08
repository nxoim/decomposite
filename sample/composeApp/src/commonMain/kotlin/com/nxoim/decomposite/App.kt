package com.nxoim.decomposite

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PanoramaVertical
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nxoim.decomposite.core.common.navigation.NavController
import com.nxoim.decomposite.core.common.navigation.NavHost
import com.nxoim.decomposite.core.common.navigation.animations.cleanSlideAndFade
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.ui.screens.heart.HeartNavHost
import com.nxoim.decomposite.ui.screens.star.StarNavHost
import com.nxoim.decomposite.ui.screens.tikitoki.TikitokiScreen
import kotlinx.serialization.Serializable


@Composable
fun App() = RootNavHost()

@Composable
fun RootNavHost() {
    val rootNavController = navController<RootDestinations>(RootDestinations.Star)

    Scaffold(bottomBar = { GlobalSampleNavBar(rootNavController) }) { scaffoldPadding ->
        NavHost<RootDestinations>(
            rootNavController,
            Modifier.padding(bottom = scaffoldPadding.calculateBottomPadding()),
            animations = {
                cleanSlideAndFade(
                    orientation = Orientation.Vertical,
                    targetOffsetDp = -16
                )
            }
        ) {
            when (it) { // nested
                RootDestinations.Star -> StarNavHost()
                RootDestinations.Tikitoki -> TikitokiScreen()
                RootDestinations.Heart -> HeartNavHost()
            }
        }
    }
}


@Composable
fun GlobalSampleNavBar(
    navController: NavController<RootDestinations>
) {
    val currentScreen = navController.currentDestination

    NavigationBar {
        NavigationBarItem(
            selected = currentScreen is RootDestinations.Star,
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            onClick = { navController.navigate(RootDestinations.Star, removeIfIsPreceding = false) }
        )

        NavigationBarItem(
            selected = currentScreen is RootDestinations.Tikitoki,
            icon = { Icon(Icons.Default.PanoramaVertical, contentDescription = null) },
            onClick = { navController.navigate(RootDestinations.Tikitoki, removeIfIsPreceding = false) }
        )

        NavigationBarItem(
            selected = currentScreen is RootDestinations.Heart,
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            onClick = { navController.navigate(RootDestinations.Heart, removeIfIsPreceding = false) }
        )
    }
}

@Serializable
sealed interface RootDestinations {
    @Serializable
    data object Star : RootDestinations

    @Serializable
    data object Tikitoki : RootDestinations

    @Serializable
    data object Heart : RootDestinations
}
