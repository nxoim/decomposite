package com.number869.decomposite.ui.screens.tikitoki

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.number869.decomposite.core.common.navigation.getExistingNavControllerInstance

@Composable
fun UserPage(mockUser: MockUser) = Surface {
    Scaffold(
        Modifier.fillMaxSize(),
        topBar = { UserPageTopAppBar(mockUser.username) }
    ) { scaffoldPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(scaffoldPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .size(128.dp)
                    .background(Color(mockUser.profilePicColorARGB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            Text(mockUser.username, style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(8.dp))

            Text(mockUser.bio, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(mockUser.followersCount.toString(), style = MaterialTheme.typography.headlineSmall)
                    Text("Followers", style = MaterialTheme.typography.bodySmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(mockUser.followingCount.toString(), style = MaterialTheme.typography.headlineSmall)
                    Text("Following", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPageTopAppBar(
    userName: String
) {
    val navController = getExistingNavControllerInstance<TikitokiDestinations>()

    CenterAlignedTopAppBar(
        title = { Text(userName) },
        navigationIcon = {
            IconButton(onClick = {navController.navigateBack()}) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}