package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.arkivanov.essenty.backhandler.BackEvent
import com.number869.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ContentAnimatorScope {
    val indexFromTop: Int
    val index: Int
    val backEvent: BackEvent
    val animationProgress: Float
    val gestureAnimationProgress: Float
    val swipeOffset: Offset
    val animationStatus: AnimationStatus
    suspend fun onBackGesture(backGesture: BackGestureEvent): Any
    suspend fun updateCurrentIndexAndAnimate(newIndex: Int, newIndexFromTop: Int, animate: Boolean = true)
    suspend fun updateStatus(
        previousLocation: ItemLocation,
        newItemLocation: ItemLocation,
        newDirection: Direction,
        newType: AnimationType
    )
}

@Immutable
class DefaultContentAnimatorScope(
    initialIndex: Int,
    initialIndexFromTop: Int,
    private val animationSpec: AnimationSpec<Float>
) : ContentAnimatorScope {
    private val mutex = Mutex()
    private var _indexFromTop by mutableIntStateOf(initialIndexFromTop)
    override val indexFromTop get() = _indexFromTop
    private var allowRemoval by mutableStateOf(true)
    private var _index by mutableIntStateOf(initialIndex)
    override val index get() = _index
    private val initial get() = _index == 0

    private val location
        get() = when {
            _indexFromTop == -1 -> ItemLocation.Outside
            _indexFromTop == 0 -> ItemLocation.Top
            _indexFromTop >= 1 -> ItemLocation.Back
            else -> error("how")
        }

    // standard progress
    private val animationProgressAnimatable = Animatable(if (initial) 0f else -1f)

    // progress - gesture progress
    private val gestureAnimationProgressAnimatable = Animatable(animationProgressAnimatable.value)

    // only for velocity
    private val rawGestureProgress = Animatable(0f)

    private var _backEvent by mutableStateOf(BackEvent())
    override val backEvent get() = _backEvent
    private var initialSwipeOffset by mutableStateOf(Offset.Zero)

    override val animationProgress by animationProgressAnimatable.asState()
    override val gestureAnimationProgress by gestureAnimationProgressAnimatable.asState()

    override val swipeOffset get() = Offset(
        initialSwipeOffset.x - _backEvent.touchX,
        initialSwipeOffset.y - _backEvent.touchY
    )

    private var _animationStatus by mutableStateOf(
        AnimationStatus(
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

                // if item is about to be removed - this prevents it from happening
                allowRemoval = false
                initialSwipeOffset = Offset(backGesture.event.touchX, backGesture.event.touchY)
                _backEvent = backGesture.event

                updateStatus(_animationStatus.location, location, newType = AnimationType.Gestures, newDirection = Direction.Outward)
            }

            is BackGestureEvent.OnBackProgressed -> {
                _backEvent = backGesture.event

                gestureAnimationProgressAnimatable.snapTo(animationProgress - backGesture.event.progress)

                launch { rawGestureProgress.animateTo(backGesture.event.progress) }
            }

            BackGestureEvent.None,
            BackGestureEvent.OnBackCancelled -> {
                allowRemoval = false
                updateStatus(_animationStatus.location, location, newType = AnimationType.Passive, newDirection = Direction.Inward)
                animateToTarget()
                allowRemoval = true
            }

            BackGestureEvent.OnBack -> {
                allowRemoval = true
                updateStatus(_animationStatus.location, location, newType = AnimationType.Passive, newDirection = Direction.Outward)
            }
        }
    }

    override suspend fun updateCurrentIndexAndAnimate(
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
            newIndexFromTop == -1 -> ItemLocation.Outside
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
        launch {
            gestureAnimationProgressAnimatable.animateTo(
                targetValue = _indexFromTop.toFloat(),
                animationSpec = animationSpec,
                initialVelocity = 0.1f + rawGestureProgress.velocity
            )
        }

        animationProgressAnimatable.animateTo(
            targetValue = _indexFromTop.toFloat(),
            animationSpec = animationSpec,
            initialVelocity = 0.1f + rawGestureProgress.velocity
        )

        launch {
            // for a moment this block will be called upon OnBack because that's animateTo's
            // intended behavior, meaning these will be called unintentionally, unintentionally
            // updating animation status. adding a delay compensates for this
            withFrameNanos {  }
            updateStatus(_animationStatus.location, location, newType = AnimationType.None, newDirection = Direction.None)
            _backEvent = BackEvent()
        }
    }

    override suspend fun updateStatus(
        previousLocation: ItemLocation,
        newItemLocation: ItemLocation,
        newDirection: Direction,
        newType: AnimationType
    ) {
        mutex.withLock {
            _animationStatus = AnimationStatus(
                previousLocation = previousLocation,
                location = newItemLocation,
                direction = newDirection,
                type = newType
            )
        }
    }
}

