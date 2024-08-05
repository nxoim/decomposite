
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.nxoim.decomposite.core.common.navigation.FallbackNavigationRootImplementation
import com.nxoim.decomposite.core.common.navigation.LocalNavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavHost
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.navigation.NavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.animations.cleanSlideAndFade
import com.nxoim.decomposite.core.common.navigation.animations.iosLikeSlide
import com.nxoim.decomposite.core.common.navigation.animations.materialContainerMorph
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.core.common.ultils.InOverlay
import org.junit.Rule
import org.junit.Test

class NavHostTests {
	@get:Rule
	val composeTestRule = createComposeRule()

	@OptIn(FallbackNavigationRootImplementation::class)
	@Test
	fun test() {
		composeTestRule.mainClock.autoAdvance = false
		println("creating navigation root data")
		val navigationRootData = NavigationRootData()

		composeTestRule.setContent {
			remember { println("creating navigation root provider") }
			NavigationRootProvider(navigationRootData) {
				remember { println("creating navigation controller") }
				val navController = navController<TestDestinations>(TestDestinations.A)
				val screenInfo = LocalNavigationRoot.current.screenInformation

				InOverlay {
					Box {
						remember { println("creating navigation host") }
						NavHost(
							navController,
							animations = {
								when (currentChild) {
									TestDestinations.A -> cleanSlideAndFade()
									TestDestinations.B -> iosLikeSlide()
									is TestDestinations.Random -> materialContainerMorph(screenInfo)
								}
							},
						) { destination ->
							when (destination) {
								TestDestinations.A -> {
									remember { println("Displaying $destination") }
								}

								TestDestinations.B -> {
									remember { println("Displaying $destination") }
								}

								is TestDestinations.Random -> {
									remember { println("Displaying $destination") }
								}
							}
						}

						Column {
							Button(
								onClick = { navController.navigate(TestDestinations.Random()) },
								modifier = Modifier.testTag("navigate-to-random")
							) {
								Text("Navigate to Random")
							}

							Button(
								onClick = { navController.navigate(TestDestinations.A) },
								modifier = Modifier.testTag("navigate-to-a")
							) {
								Text("Navigate to A")
							}

							Button(
								onClick = { navController.navigate(TestDestinations.B) },
								modifier = Modifier.testTag("navigate-to-b")
							) {
								Text("Navigate to B")
							}

							Button(
								onClick = { navController.navigateBack() },
								modifier = Modifier.testTag("navigate-back")
							) {
								Text("Navigate back")
							}

							Button(
								onClick = {
									navController.navigateBackTo(TestDestinations.A)
								},
								modifier = Modifier.testTag("navigate-back-to-a")
							) {
								Text("Navigate back to A")
							}

							Button(
								onClick = {
									navController.replaceAll(TestDestinations.A, TestDestinations.B)
								},
								modifier = Modifier.testTag("replace-all")
							) {
								Text("Replace all with A and B")
							}

							Button(
								onClick = {
									navController.replaceCurrent(TestDestinations.Random())
								},
								modifier = Modifier.testTag("replace-current")
							) {
								Text("Replace current with Random")
							}
						}
					}
				}
			}
		}

		println("adding 50 random destinations")

		composeTestRule.runOnUiThread {
			repeat(50) {
				composeTestRule
					.onNodeWithTag("navigate-to-random")
					.performClick()

				val navigateBack = kotlin.random.Random.nextBoolean()

				if (navigateBack) {
					println("navigating back randomly")
					composeTestRule
						.onNodeWithTag("navigate-back")
						.performClick()
				}
			}

			println("replacing all with A and B")
			composeTestRule
				.onNodeWithTag("replace-all")
				.performClick()


			println("replacing current with Random")
			composeTestRule
				.onNodeWithTag("replace-current")
				.performClick()


			println("testing navigateBackTo")
			composeTestRule
				.onNodeWithTag("navigate-back-to-a")
				.performClick()

			composeTestRule
				.onNodeWithTag("navigate-back")
				.performClick()
		}
	}
}

private sealed interface TestDestinations {
	data object A : TestDestinations
	data object B : TestDestinations
	data class Random(val int: Int = kotlin.random.Random.nextInt()) : TestDestinations
}