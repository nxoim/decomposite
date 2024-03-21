package com.number869.decomposite.ui.screens.star.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.number869.decomposite.core.common.navigation.getExistingNavControllerInstance
import com.number869.decomposite.core.common.ultils.ContentType
import com.number869.decomposite.ui.screens.star.StarDestinations

@Composable
fun StarHomeScreen() {
    val navController = getExistingNavControllerInstance<StarDestinations>()

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Star",
            modifier = Modifier.size(80.dp)
        )

        OutlinedButton(
            onClick = {
                navController.navigate(
                    StarDestinations.AnotherStar,
                    ContentType.Overlay
                )
            }
        ) {
            Text("See another star")
        }
    }
}