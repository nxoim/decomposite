package com.nxoim.decomposite.ui.screens.tikitoki

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import com.nxoim.decomposite.core.common.navigation.NavHost
import com.nxoim.decomposite.core.common.navigation.animations.iosLikeSlide
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.core.common.viewModel.viewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TikitokiScreen() = SharedTransitionLayout {
    // initializing the view model here, and will be using the getter everywhere else
    viewModel() { TikitokiViewModel() }

    NavHost(
        navController<TikitokiDestinations>(TikitokiDestinations.Pager),
        animations = { iosLikeSlide() }
    ) {
        when (it) {
            TikitokiDestinations.Pager -> ListPager(
                animatedVisibilityScope = this,
                sharedTransitionScope = this@SharedTransitionLayout
            )
            is TikitokiDestinations.User -> UserPage(
                mockUser = it.mockUser,
                animatedVisibilityScope = this,
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
    }
}

