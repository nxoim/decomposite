package com.number869.decomposite.ui.screens.tikitoki

import kotlinx.serialization.Serializable

data class MockPage(val name: String, val mockUser: MockUser)
@Serializable
data class MockUser(val username: String)