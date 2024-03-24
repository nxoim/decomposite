package com.nxoim.decomposite.ui.screens.heart.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nxoim.decomposite.core.common.navigation.getExistingNavController
import com.nxoim.decomposite.ui.screens.heart.HeartDestinations

@Composable
fun HeartHomeScreen() {
    val navController = getExistingNavController<HeartDestinations>()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Heart",
            modifier = Modifier.size(64.dp)
        )

        OutlinedButton(
            onClick = { navController.navigate(HeartDestinations.AnotherHeart("some text from an argument")) }
        ) {
            Text("Open another heart screen")
        }
    }
}