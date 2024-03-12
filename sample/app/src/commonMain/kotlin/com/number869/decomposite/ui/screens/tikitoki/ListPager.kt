package com.number869.decomposite.ui.screens.tikitoki

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.number869.decomposite.core.common.navigation.navController
import com.number869.decomposite.core.common.ultils.noRippleClickable
import com.number869.decomposite.core.common.viewModel.getExistingViewModelInstance

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListPager() {
    val vm = getExistingViewModelInstance<TikitokiViewModel>()
    val pagerState = rememberPagerState() { vm.mockVids.size }

    Box() {
        VerticalPager(pagerState) { PageStuff(vm.mockVids[it]) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageStuff(mockPage: MockPage) {
    val navController = navController<TikitokiDestinations>()
    var displayComments by remember { mutableStateOf(false) }
    var displayOptions by remember { mutableStateOf(false) }
    val commentsSheetState = rememberModalBottomSheetState()

    BoxWithConstraints {
        val animatedFillFraction by animateFloatAsState(
            if (commentsSheetState.targetValue != SheetValue.Hidden) 0.5f else 1f
        )

        Box(
            Modifier
                .background(MaterialTheme.colorScheme.surfaceDim)
                .fillMaxSize()
                .noRippleClickable() { displayOptions = true }
        ) {
            Box(Modifier.fillMaxHeight(animatedFillFraction).fillMaxWidth()) {
                Text(
                    mockPage.name,
                    modifier = Modifier.align(Alignment.Center)
                )

                AnimatedVisibility(
                    !displayComments,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { navController.navigate(TikitokiDestinations.User(mockPage.mockUser)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }

                        Spacer(Modifier.height(8.dp))

                        IconButton(onClick = { displayComments = true }) {
                            Icon(Icons.Default.Message, contentDescription = null)
                        }
                    }
                }
            }


            if (displayOptions) ModalBottomSheet(
                onDismissRequest = { displayOptions = false },
                windowInsets = CorrectBottomSheetWindowInsets()
            ) {
                Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Options for $mockPage")
                }
            }

            if (displayComments) ModalBottomSheet(
                sheetState = commentsSheetState,
                onDismissRequest = { displayComments = false },
                windowInsets = CorrectBottomSheetWindowInsets()
            ) {
                Box(
                    Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Comments for $mockPage")
                }
            }
        }
    }
}

@Composable
private fun CorrectBottomSheetWindowInsets(): WindowInsets {
    val currentInsets = WindowInsets.statusBars.asPaddingValues()

    return remember {
        WindowInsets(
            left = 0.dp,
            top = currentInsets.calculateTopPadding(),
            right = 0.dp,
            bottom = 0.dp
        )
    }
}