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
class ContentAnimatorScope(initialIndex: Int, initialIndexFromTop: Int) {
    private val mutex = Mutex()
    internal var animationSpec by mutableStateOf<AnimationSpec<Float>>(softSpring())
    private var _indexFromTop by mutableIntStateOf(initialIndexFromTop)
    val indexFromTop get() = _indexFromTop
    private var allowRemoval by mutableStateOf(false)
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

    internal var allowAnimation by mutableStateOf(true)

    /**
     * Will allow the display of content if [untilIndexFromTop] is below or equal indexFromTop.
     * If is below - will only render if is animated, and if is 0 or -1 (at the top of the
     * stack or outside) - will always render
     */
    @Composable
    fun renderUntil(untilIndexFromTop: Int) = remember(_indexFromTop) {
        location.top || location.outside || (_indexFromTop <= untilIndexFromTop && animationStatus.animating)
    }.also { allowAnimation = it }

    internal suspend fun onBackGesture(backGesture: BackGestureEvent) = coroutineScope {
        when (backGesture) {
            is BackGestureEvent.OnBackStarted -> {
                // stop all animations
                animationProgressAnimatable.stop()
                gestureAnimationProgressAnimatable.stop()

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
                updateStatus(AnimationType.Passive, Direction.Inward)
                animateToTarget()
                allowRemoval = false
            }

            BackGestureEvent.OnBack -> {
                updateStatus(AnimationType.None, Direction.None)
                animateToTarget()
                allowRemoval = true
            }
        }
    }

    internal suspend fun updateCurrentIndex(newIndex: Int, newIndexFromTop: Int) {
        val direction = when {
            newIndexFromTop > _indexFromTop -> Direction.Inward
            newIndexFromTop < _indexFromTop -> Direction.Outward
            else -> animationStatus.direction
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
    }

    internal suspend fun animateToTarget() = coroutineScope {
        if (allowAnimation) {
            launch {
                animationProgressAnimatable.animateTo(
                    targetValue = _indexFromTop.toFloat(),
                    animationSpec = animationSpec,
                    initialVelocity = rawGestureProgress.velocity
                )

                updateStatus(AnimationType.None, Direction.None)
                if (location.outside && allowRemoval) removalRequestChannel.emit(true)
                _backEvent = BackEvent()
            }

            launch {
                gestureAnimationProgressAnimatable.animateTo(
                    targetValue = _indexFromTop.toFloat(),
                    animationSpec = animationSpec,
                    initialVelocity = rawGestureProgress.velocity
                )
            }
        }
    }

    private suspend fun updateStatus(type: AnimationType, direction: Direction, newItemLocation: ItemLocation? = null) {
        mutex.withLock {
            _animationStatus = AnimationStatus(
                previousLocation = animationStatus.location,
                location = newItemLocation ?: location,
                direction = direction,
                type = type
            )
        }
    }
}

