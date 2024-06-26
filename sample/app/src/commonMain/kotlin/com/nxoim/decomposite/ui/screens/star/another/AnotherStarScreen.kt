package com.nxoim.decomposite.ui.screens.star.another

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nxoim.decomposite.core.common.navigation.getExistingNavController
import com.nxoim.decomposite.core.common.navigation.snacks.snackController
import com.nxoim.decomposite.core.common.ultils.ContentType
import com.nxoim.decomposite.core.common.ultils.LocalContentType
import com.nxoim.decomposite.ui.screens.star.StarDestinations
import kotlin.time.Duration.Companion.seconds

@Composable
fun AnotherStarScreen() {
    val contentType = LocalContentType.current
    val navController = getExistingNavController<StarDestinations>()
    val snackController = snackController()

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

    LaunchedEffect(Unit) {
        if (contentType == ContentType.Overlay) snackController.display(
            "noStarScreenSnack",
            displayDurationMillis = 2L.seconds
        ) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Btw this is a snack and this screen is open as an overlay",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}