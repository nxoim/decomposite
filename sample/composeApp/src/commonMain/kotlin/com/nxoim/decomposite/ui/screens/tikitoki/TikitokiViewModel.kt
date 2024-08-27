package com.nxoim.decomposite.ui.screens.tikitoki

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.nxoim.decomposite.core.common.viewModel.ViewModel
import kotlin.random.Random

class TikitokiViewModel() : ViewModel() {
    val mockVids = mutableStateListOf<MockPage>().apply {
        repeat(15) {
            val randomColor = Color(
                red = Random.nextFloat(),
                green = Random.nextFloat(),
                blue = Random.nextFloat()
            ).toArgb()
            val username = "User${it + 1}"

            val mockUser = MockUser(
                username = username,
                profilePicColorARGB = randomColor,
                bio = "Bio of $username",
                followersCount = Random(66).nextInt() / 1000,
                followingCount = Random(4).nextInt() / 1000
            )

            add(MockPage("Video ${it + 1}", mockUser))
        }
    }

    override fun onDestroy(removeFromViewModelStore: () -> Unit) {
        // do nothing
    }
}