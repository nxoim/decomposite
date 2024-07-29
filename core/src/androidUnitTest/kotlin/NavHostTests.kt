
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.arkivanov.decompose.defaultComponentContext
import com.nxoim.decomposite.core.android.navigation.NavigationRootProvider
import com.nxoim.decomposite.core.common.navigation.NavigationRootData
import com.nxoim.decomposite.core.common.navigation.navController
import org.junit.Rule
import org.junit.Test

class NavHostTests {
	@get:Rule
	val composeTestRule = createComposeRule()

	// Cannot invoke "String.toLowerCase(java.util.Locale)" because "android.os.Build.FINGERPRINT" is null
	@SuppressLint("RememberReturnType")
	@Test
	fun configChanges() {
		val stateTester = StateRestorationTester(composeTestRule)

		object : ComponentActivity() {
			val navigationRootData = NavigationRootData(defaultComponentContext())

			override fun onCreate(savedInstanceState: Bundle?) {
				super.onCreate(savedInstanceState)
				stateTester.setContent {
					NavigationRootProvider(navigationRootData) {
						remember { println("creating a navigation controller") }
						navController<Int>(0)
					}
				}
			}
		}

		stateTester.emulateSavedInstanceStateRestore()
	}
}