package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.*
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

internal class MaterialContainerMorphContentAnimatorScope (
    initialIndex: Int,
    initialIndexFromTop: Int,
    private val animationSpec: AnimationSpec<Float>,
) : ContentAnimatorScope {
    private val mutex = Mutex()
    private var _indexFromTop by mutableIntStateOf(initialIndexFromTop)
    override val indexFromTop get() = _indexFromTop
    private var _index by mutableIntStateOf(initialIndex)
    override val index get() = _index
    private val initial get() = _index == 0

    private val location
        get() = when {
            _indexFromTop <= -1 -> ItemLocation.Outside
            _indexFromTop == 0 -> ItemLocation.Top
            _indexFromTop >= 1 -> ItemLocation.Back
            else -> error("how")
        }

    // standard progress
    private val animationProgressAnimatable = Animatable(indexFromTop.toFloat())
    // progress - gesture progress
    private val gestureAnimationProgressAnimatable = Animatable(indexFromTop.toFloat())
    private val swipeOffsetAnimatable = Animatable(IntOffset.Zero, IntOffset.VectorConverter)

    private val _swipeEdge = mutableStateOf(BackEvent.SwipeEdge.LEFT)

    // only for velocity
    private val rawGestureProgress = Animatable(0f)

    private var initialSwipeOffset by mutableStateOf(Offset.Zero)

    val animationProgress by animationProgressAnimatable.asState()
    val gestureAnimationProgress by gestureAnimationProgressAnimatable.asState()
    val swipeOffset by swipeOffsetAnimatable.asState()
    val swipeEdge by _swipeEdge

    private var _animationStatus by mutableStateOf(
        DefaultAnimationStatus(
            previousLocation = if (initial) ItemLocation.Top else ItemLocation.Outside,
            location = ItemLocation.Top,
            direction = if (initial) Direction.None else Direction.Inward,
            type = if (initial) AnimationType.None else AnimationType.Passive
        )
    )

    override val animationStatus get() = _animationStatus

    override suspend fun onBackGesture(backGesture: BackGestureEvent) = coroutineScope {
        when (backGesture) {
            is BackGestureEvent.OnBackStarted -> {
                // stop all animations
                animationProgressAnimatable.stop()
                gestureAnimationProgressAnimatable.stop()
                swipeOffsetAnimatable.stop()

                initialSwipeOffset = Offset(backGesture.event.touchX, backGesture.event.touchY)
                _swipeEdge.value = backGesture.event.swipeEdge

                updateStatus(
                    _animationStatus.location,
                    location,
                    Direction.Outward,
                    AnimationType.Gestures,
                )
            }

            is BackGestureEvent.OnBackProgressed -> {
                if (location.top) {
                    gestureAnimationProgressAnimatable.snapTo(animationProgress - backGesture.event.progress)
                    swipeOffsetAnimatable.snapTo(
                        IntOffset(
                            (backGesture.event.touchX - initialSwipeOffset.x).roundToInt(),
                            (backGesture.event.touchY - initialSwipeOffset.y).roundToInt()
                        )
                    )

                    launch { rawGestureProgress.animateTo(backGesture.event.progress) }
                }

                updateStatus(
                    _animationStatus.location,
                    location,
                    Direction.Outward,
                    AnimationType.Gestures,
                )
            }

            BackGestureEvent.None,
            BackGestureEvent.OnBackCancelled -> {
                updateStatus(
                    _animationStatus.location,
                    location,
                    Direction.Inward,
                    AnimationType.Passive,
                )
                animateToTarget()
            }

            BackGestureEvent.OnBack -> {
                if (location.outside) updateStatus(
                    _animationStatus.location,
                    location,
                    Direction.None,
                    AnimationType.None
                ) else updateStatus(
                    _animationStatus.location,
                    location,
                    Direction.Outward,
                    AnimationType.Passive
                )
            }
        }
    }

    override suspend fun update(
        newIndex: Int,
        newIndexFromTop: Int,
        animate: Boolean
    ) {
        val newDirection = when {
            newIndexFromTop > _indexFromTop -> Direction.Inward
            newIndexFromTop < _indexFromTop -> Direction.Outward
            else -> _animationStatus.direction
        }

        val newLocation = when {
            newIndexFromTop <= -1 -> ItemLocation.Outside
            newIndexFromTop == 0 -> ItemLocation.Top
            newIndexFromTop >= 1 -> ItemLocation.Back
            else -> error("how")
        }

        val previousLocation = if (newIndexFromTop == 0 && newDirection == Direction.Inward) {
            ItemLocation.Outside
        } else {
            location
        }

        _index = newIndex
        _indexFromTop = newIndexFromTop

        if (animate) {
            updateStatus(previousLocation, newLocation, newDirection, AnimationType.Passive)
            animateToTarget()
        } else {
            updateStatus(previousLocation, newLocation, Direction.None, AnimationType.None)
        }
    }

    private suspend fun animateToTarget() = coroutineScope {
        // if the location is outside - report that a removal from the screen is needed by
        // not animating the progress, as animateTo delays that action
        if (!location.outside) {
            launch {
                gestureAnimationProgressAnimatable.animateTo(
                    targetValue = (_indexFromTop.coerceAtLeast(-1)).toFloat(),
                    animationSpec = animationSpec,
                    initialVelocity = 0.1f + rawGestureProgress.velocity
                )
            }

            launch { swipeOffsetAnimatable.animateTo(targetValue = IntOffset.Zero) }

            animationProgressAnimatable.animateTo(
                targetValue = (_indexFromTop.coerceAtLeast(-1)).toFloat(),
                animationSpec = animationSpec,
                initialVelocity = 0.1f + rawGestureProgress.velocity
            )
        }

        launch {
            // for a moment this block will be called upon OnBack because that's animateTo's
            // intended behavior, meaning these will be called unintentionally, unintentionally
            // updating animation status. adding a delay compensates for this.

            // dont need this delay if the item is to be removed, which is when location is outside
            if (!location.outside) withFrameNanos {  }

            updateStatus(
                _animationStatus.location,
                location,
                Direction.None,
                AnimationType.None
            )
        }
    }

    private suspend fun updateStatus(
        previousLocation: ItemLocation,
        newItemLocation: ItemLocation,
        newDirection: Direction,
        newType: AnimationType
    ) {
        mutex.withLock {
            _animationStatus = DefaultAnimationStatus(
                previousLocation = previousLocation,
                location = newItemLocation,
                direction = newDirection,
                type = newType
            )
        }
    }
}