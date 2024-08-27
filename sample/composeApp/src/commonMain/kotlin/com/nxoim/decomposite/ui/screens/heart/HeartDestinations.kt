package com.nxoim.decomposite.ui.screens.heart

import kotlinx.serialization.Serializable

@Serializable
sealed interface HeartDestinations {
    @Serializable
    data object Home : HeartDestinations

    @Serializable
    data class AnotherHeart(val text: String) : HeartDestinations
}