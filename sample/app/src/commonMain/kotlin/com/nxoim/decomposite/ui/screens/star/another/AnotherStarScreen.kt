package com.nxoim.decomposite.ui.screens.star.another

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nxoim.decomposite.core.common.navigation.getExistingNavController
import com.nxoim.decomposite.ui.screens.star.StarDestinations

@Composable
fun AnotherStarScreen() {
    val navController = getExistingNavController<StarDestinations>()

    Surface {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text(
                "I lied, there is no star here muehehehehehehe >:3",
                style = MaterialTheme.typography.displaySmall,
            )

            Button(
                onClick = { navController.navigateBack() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Go back")
            }
        }
    }
}