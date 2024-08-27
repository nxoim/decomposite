package com.nxoim.decomposite.ui.screens.tikitoki

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nxoim.decomposite.core.common.navigation.NavController
import com.nxoim.decomposite.core.common.viewModel.getExistingViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ListPager(
	animatedVisibilityScope: AnimatedVisibilityScope,
	sharedTransitionScope: SharedTransitionScope,
	navController: NavController<TikitokiDestinations>
){
	val vm = getExistingViewModel<TikitokiViewModel>()
	val pagerState = rememberPagerState() { vm.mockVids.size }

	Box() {
		VerticalPager(pagerState) {
			PageStuff(vm.mockVids[it], animatedVisibilityScope, sharedTransitionScope, navController)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun PageStuff(
	mockPage: MockPage,
	animatedVisibilityScope: AnimatedVisibilityScope,
	sharedTransitionScope: SharedTransitionScope,
	navController: NavController<TikitokiDestinations>
) = with(sharedTransitionScope) {
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
								.sharedBounds(
									rememberSharedContentState(
										TikitokiSharedElements.ProfilePicture(mockPage.mockUser)
									),
									animatedVisibilityScope
								)
								.clip(CircleShape)
								.size(64.dp)
								.background(Color(mockPage.mockUser.profilePicColorARGB))
								.clickable {
									navController.navigate(
										TikitokiDestinations.User(
											mockPage.mockUser
										)
									)
								},
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
				contentWindowInsets = { CorrectBottomSheetWindowInsets() }
			) {
				Box(
					Modifier.fillMaxWidth().height(200.dp),
					contentAlignment = Alignment.Center
				) {
					Text("Options for ${mockPage.name}")
				}
			}

			if (displayComments) ModalBottomSheet(
				sheetState = commentsSheetState,
				onDismissRequest = { displayComments = false },
				contentWindowInsets = { CorrectBottomSheetWindowInsets() }
			) {
				Box(
					Modifier.fillMaxWidth().fillMaxHeight(0.5f),
					contentAlignment = Alignment.Center
				) {
					Text("Comments for ${mockPage.name}")
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

@Stable
private fun Modifier.noRippleClickable(onClick: () -> Unit = {}) = this.clickable(
	indication = null,
	interactionSource = MutableInteractionSource(),
	onClick = onClick
)