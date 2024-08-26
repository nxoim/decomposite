
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType
import com.nxoim.decomposite.core.common.navigation.animations.Direction
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation
import com.nxoim.decomposite.core.common.navigation.animations.scopes.DefaultContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.softSpring
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class DefaultContentAnimatorCreatorScopeTests {
	@get:Rule
	val rule = createComposeRule()

	@Test
	fun initialItemAnimationStatusCorrect() {
		val scope = DefaultContentAnimator(0, 0, softSpring())
		assert(scope.animationStatus == correctInitialAnimationStatus)
	}

	@Test
	fun newItemAnimationStatusCorrect() {
		val scope = DefaultContentAnimator(1, 0, softSpring())
		assert(scope.animationStatus == correctNewAnimationStatus)
	}

	@Test
	fun gestureUpdateItemAnimationStatusCorrect() {
		val scope = DefaultContentAnimator(1, 0, softSpring())

		rule.setContent {
			val coroutineScope = rememberCoroutineScope()

			remember {
				coroutineScope.launch {
					scope.onBackGesture(
						BackGestureEvent.OnBackStarted(BackEvent(swipeEdge = BackEvent.SwipeEdge.LEFT))
					)
					assert(scope.animationStatus.animationType == AnimationType.Gestures)
					assert(scope.animationStatus.direction == Direction.Outwards)
					println("onBackStarted passed. preparing to test onBackProgressed")

					scope.onBackGesture(
						BackGestureEvent.OnBackProgressed(BackEvent(progress = 0.5F))
					)

					assert(scope.animationStatus.animationType == AnimationType.Gestures)
					assert(scope.animationStatus.direction == Direction.Outwards)
					println("onBackProgressed passed. preparing to test onBackCancelled")

					launch { scope.onBackGesture(BackGestureEvent.OnBackCancelled) }
					delay(16)
					assert(scope.animationStatus.animationType == AnimationType.PassiveCancelling)
					assert(scope.animationStatus.direction == Direction.Inwards)
					println("onBackCancelled passed. preparing to test onBack")

					scope.onBackGesture(BackGestureEvent.OnBack)
					assert(scope.animationStatus.animationType == AnimationType.Passive)
					assert(scope.animationStatus.direction == Direction.Outwards)
					println("onBack passed")
				}
			}
		}
	}

	// is supposed to fail if the animation status is incorrect. the animation
	// status data class has a check in the init block
	@Test
	fun updateItemAnimationStatusCorrect() {
		val scope = DefaultContentAnimator(0, 0, softSpring())

		rule.setContent {
			val coroutineScope = rememberCoroutineScope()

			remember {
				coroutineScope.launch {
					println("testing inwards once")
					scope.update(0, 1)

					println("testing outwards once")
					scope.update(0, 0)

					repeat(20) { indexFromTop ->
						delay(Random.nextLong(until = 400).coerceAtLeast(50))
						launch {
							println("inwards fast $indexFromTop")
							scope.update(0, indexFromTop)
						}
					}

					println("testing outwards fast")
					scope.update(0, 0)

					// random fast
					println("testing random fast")
					repeat(20) {
						delay(Random.nextLong(until = 400).coerceAtLeast(50))

						launch {
							scope.update(
								Random.nextInt(from = 0, until = 20),
								Random.nextInt(from = 0, until = 20)
							)
						}
					}
				}
			}
		}
	}
}

// when the stack appears for the first time
private val correctInitialAnimationStatus = AnimationStatus(
	previousLocation = ItemLocation.Top,
	location = ItemLocation.Top,
	direction = Direction.None,
	animationType = AnimationType.None
)

// when adding an item to the stack
private val correctNewAnimationStatus = AnimationStatus(
	previousLocation = ItemLocation.Outside(-1),
	location = ItemLocation.Top,
	direction = Direction.Inwards,
	animationType = AnimationType.Passive
)