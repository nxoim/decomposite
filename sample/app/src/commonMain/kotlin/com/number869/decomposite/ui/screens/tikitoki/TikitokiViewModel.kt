package com.number869.decomposite.ui.screens.tikitoki

import androidx.compose.runtime.mutableStateListOf
import com.number869.decomposite.core.common.viewModel.ViewModel
import kotlin.random.Random

class TikitokiViewModel() : ViewModel() {
    val mockVids = mutableStateListOf<MockPage>().apply {
        repeat(10) { add(MockPage(it.toString(), MockUser(Random(0).toString()))) }
    }

    override fun onDestroy(removeFromViewModelStore: () -> Unit) {
        // do nothing
    }
}