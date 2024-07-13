package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimations
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimator
import com.nxoim.decomposite.core.common.navigation.animations.Direction
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.none
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.outside
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.top
import com.nxoim.decomposite.core.common.navigation.animations.softSpring
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * [animationSpec] is still used for the gesture cancellation animation
 */
@OptIn(InternalDecomposeApi::class)
internal fun materialContainerMorphContentAnimator(
    animationSpec: AnimationSpec<Float> = softSpring(),
    renderUntil: Int = 1,
    requireVisibilityInBackstack: Boolean = false,
    block: MaterialContainerMorphContentAnimatorScope.() -> Modifier
) = ContentAnimations(
    listOf(
        ContentAnimator(
            // 1 instance per animation spec per destination
            key = "MaterialContainerMorphContentAnimatorScope",
            renderUntil = renderUntil,
            requireVisibilityInBackstack = requireVisibilityInBackstack,
            animatorScopeFactory = { initialIndex, initialIndexFromTop ->
                MaterialContainerMorphContentAnimatorScope(
                    initialIndex,
                    initialIndexFromTop,
                    animationSpec,
                )
            },
            animationModifier = block
        )
    )
)

internal class MaterialContainerMorphContentAnimatorScope(
    private val initialIndex: Int,
    private val initialIndexFromTop: Int,
    private val animationSpec: AnimationSpec<Float>
) : ContentAnimatorScope {
    override var indexFromTop by mutableIntStateOf(initialIndexFromTop)
        private set

    override var index by mutableIntStateOf(initialIndex)
        private set

    private val initial get() = index == 0

    private var previousIndexFromTop by mutableStateOf(
        when {
            initial -> 0
            initialIndexFromTop == 0 -> -1
            else -> null
        }
    )

    private val velocityTracker = VelocityTracker()

    // standard progress
    private val animationProgressAnimatable = Animatable(indexFromTop.toFloat())
    // progress - gesture progress
    private val gestureAnimationProgressAnimatable = Animatable(indexFromTop.toFloat())
    private val swipeOffsetAnimatable = Animatable(IntOffset.Zero, IntOffset.VectorConverter)

    private var initialSwipeOffset by mutableStateOf(Offset.Zero)

    val animationProgress by animationProgressAnimatable.asState()
    val gestureAnimationProgress by gestureAnimationProgressAnimatable.asState()
    val swipeOffset by swipeOffsetAnimatable.asState()
    var swipeEdge by mutableStateOf(BackEvent.SwipeEdge.LEFT)
        private set
    // this isn't meant to be animated by gestures because container transform
    override val animationProgressForScope = animationProgressAnimatable.asState()

    private var direction by mutableStateOf(
        if (initial) Direction.None else Direction.Inwards
    )
    private var animationType by mutableStateOf(
        if (direction.none) AnimationType.None else AnimationType.Passive
    )

    override var animationStatus by mutableStateOf(
        AnimationStatus(
            previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
            location = toItemLocation(indexFromTop),
            direction = direction,
            animationType = animationType
        )
    )
        private set

    private val location
        get() = toItemLocation(indexFromTop)

    override suspend fun onBackGesture(backGesture: BackGestureEvent) = coroutineScope {
        when (backGesture) {
            is BackGestureEvent.OnBackStarted -> {
                // stop all animations
                animationProgressAnimatable.stop()
                gestureAnimationProgressAnimatable.stop()
                swipeOffsetAnimatable.stop()

                initialSwipeOffset = Offset(backGesture.event.touchX, backGesture.event.touchY)
                swipeEdge = backGesture.event.swipeEdge

                direction = Direction.Outwards
                animationType = AnimationType.Gestures
                updateAnimationStatusAfterAllChanges()
            }

            is BackGestureEvent.OnBackProgressed -> {
                if (location.top) {
                    gestureAnimationProgressAnimatable.snapTo(animationProgress - backGesture.event.progress)

                    // an animation can be kinda cancelled (see AnimationType.PassiveCancelling)
                    // meaning the direction and type might be updated to none
                    // while a gesture is in progress. this makes sure that doesn't happen
                    direction = Direction.Outwards
                    animationType = AnimationType.Gestures
                    updateAnimationStatusAfterAllChanges()

                    swipeOffsetAnimatable.snapTo(
                        IntOffset(
                            (backGesture.event.touchX - initialSwipeOffset.x).roundToInt(),
                            (backGesture.event.touchY - initialSwipeOffset.y).roundToInt()
                        )
                    )
                } else return@coroutineScope
            }

            BackGestureEvent.None,
            BackGestureEvent.OnBackCancelled -> {
                direction = Direction.Inwards
                animationType = AnimationType.PassiveCancelling

                animateToTarget()
            }

            BackGestureEvent.OnBack -> {
                direction = Direction.Outwards
                animationType = AnimationType.Passive
                // on BackGestureEvent.OnBack an item is removed, and that will
                // trigger [update] function, updating the state and triggering an animation
            }
        }
    }

    override suspend fun update(
        newIndex: Int,
        newIndexFromTop: Int,
        animate: Boolean
    ) {
        val newDirection = when {
            newIndexFromTop <= -1 || newIndexFromTop < indexFromTop -> Direction.Outwards
            newIndexFromTop > indexFromTop -> Direction.Inwards
            else -> Direction.None
        }

        previousIndexFromTop = indexFromTop

        index = newIndex
        indexFromTop = newIndexFromTop

        direction = newDirection
        animationType = if (newDirection.none) AnimationType.None else AnimationType.Passive

        if (animate) animateToTarget()
    }

    private suspend fun animateToTarget() = coroutineScope {
        updateAnimationStatusAfterAllChanges()
        // if the location is outside - report that a removal from the screen is needed by
        // not animating the progress, as animateTo delays that action

        val gestureProgressAnimation = launch {
           gestureAnimationProgressAnimatable.animateTo(
                targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
                animationSpec = animationSpec,
                initialVelocity = 0.1f + velocityTracker.calculateVelocity().x
            )
        }

        val swipeOffsetAnimation = launch {
            swipeOffsetAnimatable.animateTo(IntOffset.Zero)
        }

        val animationProgressAnimation = launch {
            animationProgressAnimatable.animateTo(
                targetValue = (indexFromTop.coerceAtLeast(-1)).toFloat(),
                animationSpec = animationSpec,
                initialVelocity = 0.1f + velocityTracker.calculateVelocity().x
            )
        }

        launch {
            // for a moment this block will be called upon OnBack because that's animateTo's
            // intended behavior, meaning these will be called unintentionally, unintentionally
            // updating animation status. adding a delay compensates for this.

            // don't need this delay if the item is to be removed, which is when location is outside
            if (!location.outside) {
                withFrameNanos { }
                joinAll(animationProgressAnimation, swipeOffsetAnimation, gestureProgressAnimation)
            }

            direction = Direction.None
            animationType = AnimationType.None
            updateAnimationStatusAfterAllChanges()
        }
    }

    private fun updateAnimationStatusAfterAllChanges() {
        animationStatus = AnimationStatus(
            previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
            location = toItemLocation(indexFromTop),
            direction = direction,
            animationType = animationType
        )
    }
}

private fun toItemLocation(indexFromTop: Int): ItemLocation = when {
    indexFromTop < 0 -> ItemLocation.Outside
    indexFromTop == 0 -> ItemLocation.Top
    indexFromTop > 0 -> ItemLocation.Back(indexFromTop)
    else -> error("Unexpected indexFromTop value: $indexFromTop")
}