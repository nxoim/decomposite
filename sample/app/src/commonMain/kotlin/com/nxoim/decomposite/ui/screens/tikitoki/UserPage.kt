package com.nxoim.decomposite.ui.screens.tikitoki

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nxoim.decomposite.core.common.navigation.NavController
import com.nxoim.decomposite.core.common.navigation.getExistingNavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun UserPage(
    mockUser: MockUser,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) = with(sharedTransitionScope) {
    Surface {
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
                        .sharedBounds(
                            rememberSharedContentState(TikitokiSharedElements.ProfilePicture(mockUser)),
                            animatedVisibilityScope
                        )
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
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPageTopAppBar(
    userName: String,
    navController: NavController<TikitokiDestinations> = getExistingNavController()!!
) {
    CenterAlignedTopAppBar(
        title = { Text(userName) },
        navigationIcon = {
            IconButton(onClick = {navController.navigateBack()}) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}