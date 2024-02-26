package com.number869.decomposite

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.number869.decomposite.core.common.navigation.NavHost
import com.number869.decomposite.core.common.navigation.NavigationRoot
import com.number869.decomposite.core.common.navigation.animations.animatedDestination
import com.number869.decomposite.core.common.navigation.animations.cleanSlideAndFade
import com.number869.decomposite.core.common.navigation.navController
import com.number869.decomposite.ui.screens.heart.HeartNavHost
import com.number869.decomposite.ui.screens.star.StarNavHost
import com.number869.decomposite.ui.theme.SampleTheme
import kotlinx.serialization.Serializable
import org.koin.compose.getKoin


@Composable
fun App() {
    SampleTheme {
        Surface {
            // because of material 3 quirks - surface wraps the root to fix text colors in overlays
            NavigationRoot(navigationRootData = getKoin().get()) { RootNavHost() }
        }
    }
}

@Composable
fun RootNavHost() = NavHost<RootDestinations>(
    startingDestination = RootDestinations.Star,
    defaultAnimation = cleanSlideAndFade(
        orientation = Orientation.Vertical,
        targetOffsetDp = -16
    ),
    routedContent = {
        Scaffold(bottomBar = { GlobalSampleNavBar() }) { scaffoldPadding ->
            it(Modifier.padding(scaffoldPadding))
        }
    },
) {
    when (it) { // nested
        RootDestinations.Star -> animatedDestination { StarNavHost() }
        RootDestinations.Heart -> animatedDestination { HeartNavHost() }
    }
}


@Composable
fun GlobalSampleNavBar() {
    val navController = navController<RootDestinations>()
    val currentScreen by navController.currentScreen.collectAsState()

    NavigationBar {
        NavigationBarItem(
            selected = currentScreen is RootDestinations.Star,
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            onClick = { navController.navigate(RootDestinations.Star)}
        )

        NavigationBarItem(
            selected = currentScreen is RootDestinations.Heart,
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            onClick = { navController.navigate(RootDestinations.Heart) }
        )
    }
}

@Serializable
sealed interface RootDestinations {
    @Serializable
    data object Star : RootDestinations

    @Serializable
    data object Heart : RootDestinations
}
