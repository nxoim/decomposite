package com.number869.decomposite.ui.screens.tikitoki

import kotlinx.serialization.Serializable

@Serializable
sealed interface TikitokiDestinations {
    @Serializable
    data object Pager : TikitokiDestinations
    @Serializable
    data class User(val mockUser: MockUser) : TikitokiDestinations
}