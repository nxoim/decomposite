package com.number869.decomposite.ui.screens.tikitoki

import androidx.compose.runtime.Composable
import com.number869.decomposite.core.common.navigation.NavHost
import com.number869.decomposite.core.common.navigation.animations.iosLikeSlide
import com.number869.decomposite.core.common.navigation.navController
import com.number869.decomposite.core.common.viewModel.viewModel

@Composable
fun TikitokiScreen() {
    val vm = viewModel() { TikitokiViewModel() }

    NavHost(
        navController<TikitokiDestinations>(TikitokiDestinations.Pager),
        animations = { iosLikeSlide() }
    ) {
        when (it) {
            TikitokiDestinations.Pager -> ListPager()
            is TikitokiDestinations.User -> UserPage()
        }
    }
}

