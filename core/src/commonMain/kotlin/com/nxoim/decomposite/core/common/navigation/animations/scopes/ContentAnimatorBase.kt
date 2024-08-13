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

	open suspend fun onGestureStarted(newBackEvent: BackEvent) { }
	open suspend fun onGestureProgressed(newBackEvent: BackEvent) { }
	open suspend fun onGestureCancelled() { }
	open suspend fun onGestureConfirmed() { }

	open suspend fun onAnimationRequested() { }
	open fun onAnimationEndedAndStatusUpdated() { }

	final override suspend fun onBackGestureStarted(backEvent: BackEvent) {
		updateAnimationStatus(
			direction = Direction.Outwards,
			animationType = AnimationType.Gestures
		)
		onGestureStarted(backEvent)
	}

	final override suspend fun onBackGestureProgressed(backEvent: BackEvent) {
		// an animation can be kinda cancelled (see AnimationType.PassiveCancelling)
		// meaning the direction and type might be updated to none
		// while a gesture is in progress. this makes sure that doesn't happen
		updateAnimationStatus(
			direction = Direction.Outwards,
			animationType = AnimationType.Gestures
		)
		onGestureProgressed(backEvent)
	}

	final override suspend fun onBackGestureCancelled() {
		updateAnimationStatus(
			direction = Direction.Inwards,
			animationType = AnimationType.PassiveCancelling
		)

		onGestureCancelled()

		launchAnimations()
	}

	final override suspend fun onBackGestureConfirmed() {
		updateAnimationStatus(
			direction = Direction.Outwards,
			animationType = AnimationType.Passive
		)

		onGestureConfirmed()
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

		launchAnimations()
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

	private suspend fun launchAnimations() = coroutineScope {
		onAnimationRequested()

		updateAnimationStatus(
			direction = Direction.None,
			animationType = AnimationType.None
		)

		onAnimationEndedAndStatusUpdated()
	}

	private fun toItemLocation(indexFromTop: Int): ItemLocation = when {
		indexFromTop < 0 -> ItemLocation.Outside(indexFromTop)
		indexFromTop == 0 -> ItemLocation.Top
		indexFromTop > 0 -> ItemLocation.Back(indexFromTop)
		else -> error("Unexpected indexFromTop value: $indexFromTop")
	}
}