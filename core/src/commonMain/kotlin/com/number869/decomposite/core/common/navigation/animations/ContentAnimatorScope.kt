package com.number869.decomposite.core.common.navigation.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.arkivanov.essenty.backhandler.BackEvent
import com.number869.decomposite.core.common.ultils.BackGestureEvent
import com.number869.decomposite.core.common.ultils.SharedBackEventScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Immutable
data class NavigationItem(
    private val initialIndex: Int,
    private val initialIndexFromTop: Int,
    internal val sharedBackEventScope: SharedBackEventScope
) {
    internal var index by mutableIntStateOf(initialIndex)
    internal var indexFromTop by mutableIntStateOf(initialIndexFromTop)
    internal var requestedRemoval by mutableStateOf<Boolean?>(null)
    internal fun updateIndex(index: Int, indexFromTop: Int) {
        this.index = index
        this.indexFromTop = indexFromTop
    }
    internal fun requestRemoval(request: Boolean?) { requestedRemoval = request }
}

@Immutable
class ContentAnimatorScope(
    initialIndex: Int,
    initialIndexFromTop: Int,
    private val animationSpec: AnimationSpec<Float>
) {
    private val mutex = Mutex()
    private var _indexFromTop by mutableIntStateOf(initialIndexFromTop)
    val indexFromTop get() = _indexFromTop
    private var allowRemoval by mutableStateOf(true)
    internal val removalRequestChannel = MutableStateFlow(false)
    private var index by mutableIntStateOf(initialIndex)
    private val initial get() = index == 0

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
    val backEvent get() = _backEvent
    private var initialSwipeOffset by mutableStateOf(Offset.Zero)

    val animationProgress by animationProgressAnimatable.asState()
    val gestureAnimationProgress by gestureAnimationProgressAnimatable.asState()

    val swipeOffset get() = Offset(
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

    val animationStatus get() = _animationStatus

    private var renderUntilIndex by mutableStateOf(Int.MAX_VALUE)
    internal val allowAnimation get() = _indexFromTop <= renderUntilIndex

    /**
     * Controls content rendering based on its position in the stack and animation state.
     * Content at or above 'untilIndexFromTop' is always rendered if it's the top or outside item (index 0 or -1).
     * If [onlyRenderBackIfAnimated] is true (which is by default) - the top and outside
     * items are rendered at all times and the backstack items (the indexes of which are below [untilIndexFromTop])
     * are only rendered if they're being animated.
     */
    @Composable
    fun renderUntil(
        untilIndexFromTop: Int,
        onlyRenderBackIfAnimated: Boolean = true
    ) = remember(index, _indexFromTop, _animationStatus.animating) {
        renderUntilIndex = untilIndexFromTop
        val disallowBackstackRender = !location.back || (allowAnimation && _animationStatus.animating)
        if (onlyRenderBackIfAnimated) disallowBackstackRender else allowAnimation
    }

    internal suspend fun onBackGesture(backGesture: BackGestureEvent) = coroutineScope {
        when (backGesture) {
            is BackGestureEvent.OnBackStarted -> {
                // stop all animations
                animationProgressAnimatable.stop()
                gestureAnimationProgressAnimatable.stop()

                // if item is about to be removed - this prevents it from happening
                allowRemoval = false
                initialSwipeOffset = Offset(backGesture.event.touchX, backGesture.event.touchY)
                _backEvent = backGesture.event

                updateStatus(AnimationType.Gestures, Direction.Outward)
            }

            is BackGestureEvent.OnBackProgressed -> {
                _backEvent = backGesture.event

                gestureAnimationProgressAnimatable.snapTo(animationProgress - backGesture.event.progress)

                launch { rawGestureProgress.animateTo(backGesture.event.progress) }
            }

            BackGestureEvent.None,
            BackGestureEvent.OnBackCancelled -> {
                allowRemoval = false
                updateStatus(AnimationType.Passive, Direction.Inward)
                animateToTarget()
                allowRemoval = true
            }

            BackGestureEvent.OnBack -> {
                allowRemoval = true
                updateStatus(AnimationType.Passive, Direction.Outward)
                animateToTarget()
            }
        }
    }

    internal suspend fun updateCurrentIndexAndAnimate(newIndex: Int, newIndexFromTop: Int) {
        val direction = when {
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
        updateStatus(AnimationType.Passive, direction, newLocation)

        index = newIndex
        _indexFromTop = newIndexFromTop

        if (allowAnimation) animateToTarget()
    }

    private suspend fun animateToTarget() = coroutineScope {
        launch {
            animationProgressAnimatable.animateTo(
                targetValue = _indexFromTop.toFloat(),
                animationSpec = animationSpec,
                initialVelocity = rawGestureProgress.velocity
            )

            launch {
                // for a moment this block will be called upon OnBack because that's animateTo's
                // intended behavior, meaning these will be called unintentionally, unintentionally
                // updating animation status. adding a delay compensates for this
                withFrameNanos {  }
                updateStatus(AnimationType.None, Direction.None)
//                if (location.outside && allowRemoval) removalRequestChannel.emit(true)
                _backEvent = BackEvent()
            }
        }

        launch {
            gestureAnimationProgressAnimatable.animateTo(
                targetValue = _indexFromTop.toFloat(),
                animationSpec = animationSpec,
                initialVelocity = rawGestureProgress.velocity
            )
        }
    }

    private suspend fun updateStatus(type: AnimationType, direction: Direction, newItemLocation: ItemLocation? = null) {
        mutex.withLock {
            _animationStatus = AnimationStatus(
                previousLocation = _animationStatus.location,
                location = newItemLocation ?: location,
                direction = direction,
                type = type
            )
        }
    }
}

