
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.nxoim.decomposite.core.common.navigation.FallbackNavigationRootImplementation
import com.nxoim.decomposite.core.common.navigation.NavigationRootProvider
import com.nxoim.decomposite.core.common.ultils.rememberRetained
import com.nxoim.decomposite.core.jvm.navigation.defaultNavigationRootData
import org.junit.Rule
import kotlin.test.Test

class RememberRetainedTests {
	@get:Rule
	val composeTestRule = createComposeRule()

	@Test
	fun providedSameKeysSameComponentContextTest() {
		composeTestRule.setContent {
			@OptIn(FallbackNavigationRootImplementation::class)
			NavigationRootProvider(defaultNavigationRootData()) {
				var visible by remember { mutableStateOf(true) }

				// initiate values
				if (visible) Box(Modifier.testTag("items")) {
					val a = rememberRetained("a") { "a" }
					val b = rememberRetained("a") { "b" }
					val c = rememberRetained("a") { "c" }

					val isValid = a == "a" && b == "b" && c == "c"

					if (!isValid) error("a: $a, b: $b, c: $c")
				}

				Button(
					onClick = { visible = !visible },
					modifier = Modifier.testTag("trigger visibility")
				) { }
			}
		}
		
		println("removing retained values from composition")
		composeTestRule.onNodeWithTag("trigger visibility").performClick()

		composeTestRule.waitUntil {
			composeTestRule
				.onAllNodesWithTag("items")
				.fetchSemanticsNodes()
				.isEmpty()
		}

		println("triggering retained values restoration")
		composeTestRule.onNodeWithTag("trigger visibility").performClick()

		composeTestRule.waitUntil {
			composeTestRule
				.onAllNodesWithTag("items")
				.fetchSemanticsNodes()
				.size == 1
		}
	}
}