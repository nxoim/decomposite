package com.nxoim.decomposite.ui.screens.star

import kotlinx.serialization.Serializable

@Serializable
sealed interface StarDestinations {
    @Serializable
    data object Home : StarDestinations

}

@Serializable
sealed interface StarOverlayDestinations {
    @Serializable
    data object Empty : StarOverlayDestinations

    @Serializable
    data object AnotherStar : StarOverlayDestinations
}