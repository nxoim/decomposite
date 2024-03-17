package com.number869.decomposite.ui.screens.tikitoki

import kotlinx.serialization.Serializable

data class MockPage(val name: String, val mockUser: MockUser)

@Serializable
data class MockUser(
    val username: String,
    val profilePicColorARGB: Int,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int
)