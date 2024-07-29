package com.nxoim.decomposite.ui.screens.tikitoki

import kotlinx.serialization.Serializable

@Serializable
sealed interface TikitokiDestinations {
    @Serializable
    data object Pager : TikitokiDestinations
    @Serializable
    data class User(val mockUser: MockUser) : TikitokiDestinations
}

sealed interface TikitokiSharedElements {
    data class ProfilePicture(val mockUser: MockUser)
}