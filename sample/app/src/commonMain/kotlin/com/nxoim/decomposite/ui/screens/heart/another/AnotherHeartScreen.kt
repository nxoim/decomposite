package com.nxoim.decomposite.ui.screens.heart.another

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nxoim.decomposite.core.common.navigation.NavController
import com.nxoim.decomposite.core.common.viewModel.ViewModel
import com.nxoim.decomposite.core.common.viewModel.viewModel
import com.nxoim.decomposite.ui.screens.heart.HeartDestinations
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun AnotherHeartScreen(
    text: String,
    navController: NavController<HeartDestinations>
) {
    val vm = viewModel(key = text) { AnotherHeartViewModel() }

    Surface {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )

            Text(text = text)
            Text("counter in the view model ${vm.counter}")

            Button(onClick = { navController.navigate(HeartDestinations.AnotherHeart(Random(999).toString())) })  {
                Text("Open another")
            }
        }
    }
}

class AnotherHeartViewModel : ViewModel() {
    var counter by mutableStateOf(0)

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                counter++
                println(counter)
            }
        }
    }

    override fun onDestroy(removeFromViewModelStore: () -> Unit) {
        // do nothing, keep the counter active
    }
}