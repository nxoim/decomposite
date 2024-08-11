package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType
import com.nxoim.decomposite.core.common.navigation.animations.Direction
import com.nxoim.decomposite.core.common.navigation.animations.Direction.Companion.none
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

abstract class ContentAnimatorBase(
	initialIndex: Int,
	initialIndexFromTop: Int
) : ContentAnimator {
	final override var indexFromTop by mutableIntStateOf(initialIndexFromTop)
		private set

	final override var index by mutableIntStateOf(initialIndex)
		private set

	private var previousIndexFromTop by mutableStateOf(
		when {
			index == 0 -> 0
			initialIndexFromTop == 0 -> -1
			else -> null
		}
	)

	final override var animationStatus by mutableStateOf(
		AnimationStatus(
			previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
			location = toItemLocation(initialIndexFromTop.coerceAtLeast(0)),
			direction = if (index == 0) Direction.None else Direction.Inwards,
			animationType = if (index == 0) AnimationType.None else AnimationType.Passive
		)
	)
		private set

	protected abstract val onBackGestures: OnGestureActions
	protected abstract val onRequestAnimationToTarget: OnAnimateToTargetRequest

	final override suspend fun onBackGesture(backGesture: BackGestureEvent) {
		when (backGesture) {
			is BackGestureEvent.OnBackStarted -> {
				updateAnimationStatus(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)
				onBackGestures.onStarted(backGesture.event)
			}

			is BackGestureEvent.OnBackProgressed -> {
				// an animation can be kinda cancelled (see AnimationType.PassiveCancelling)
				// meaning the direction and type might be updated to none
				// while a gesture is in progress. this makes sure that doesn't happen
				updateAnimationStatus(
					direction = Direction.Outwards,
					animationType = AnimationType.Gestures
				)
				onBackGestures.onProgressed(backGesture.event)
			}

			BackGestureEvent.None,
			BackGestureEvent.OnBackCancelled -> {
				updateAnimationStatus(
					direction = Direction.Inwards,
					animationType = AnimationType.PassiveCancelling
				)

				onBackGestures.onCancelled()

				launchAnimations(onRequestAnimationToTarget)
			}

			BackGestureEvent.OnBack -> {
				onBackGestures.onCompleted()

				updateAnimationStatus(
					direction = Direction.Outwards,
					animationType = AnimationType.Passive
				)
			}
		}
	}

	final override suspend fun update(newIndex: Int, newIndexFromTop: Int) {
		val newDirection = when {
			newIndexFromTop <= -1 || newIndexFromTop < indexFromTop -> Direction.Outwards
			newIndexFromTop > indexFromTop -> Direction.Inwards
			else -> Direction.None
		}

		previousIndexFromTop = indexFromTop

		index = newIndex
		indexFromTop = newIndexFromTop

		updateAnimationStatus(
			previousIndexFromTop = previousIndexFromTop,
			indexFromTop = indexFromTop,
			direction = newDirection,
			animationType = if (newDirection.none) AnimationType.None else AnimationType.Passive
		)

		launchAnimations(onRequestAnimationToTarget)
	}

	private fun updateAnimationStatus(
		previousIndexFromTop: Int? = this.previousIndexFromTop,
		indexFromTop: Int = this.indexFromTop,
		direction: Direction,
		animationType: AnimationType
	) {
		animationStatus = AnimationStatus(
			previousLocation = previousIndexFromTop?.let { toItemLocation(it) },
			location = toItemLocation(indexFromTop),
			direction = direction,
			animationType = animationType
		)
	}

	private suspend fun launchAnimations(
		animationToTarget: OnAnimateToTargetRequest
	) = coroutineScope {
		animationToTarget.animation(this)

		updateAnimationStatus(
			direction = Direction.None,
			animationType = AnimationType.None
		)

		animationToTarget.onStatusUpdate?.invoke(this)
	}

	private fun toItemLocation(indexFromTop: Int): ItemLocation = when {
		indexFromTop < 0 -> ItemLocation.Outside(indexFromTop)
		indexFromTop == 0 -> ItemLocation.Top
		indexFromTop > 0 -> ItemLocation.Back(indexFromTop)
		else -> error("Unexpected indexFromTop value: $indexFromTop")
	}

	protected class OnAnimateToTargetRequest(
		val onStatusUpdate: (suspend CoroutineScope.() -> Unit)? = null,
		val animation: suspend CoroutineScope.() -> Unit
	)

	protected class OnGestureActions(
		val onStarted: suspend (BackEvent) -> Unit = { },
		val onProgressed: suspend (BackEvent) -> Unit = { },
		val onCancelled: suspend () -> Unit = { },
		val onCompleted: suspend () -> Unit = { }
	)
}