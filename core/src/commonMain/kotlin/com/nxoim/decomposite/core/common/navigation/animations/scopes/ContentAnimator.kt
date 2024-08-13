package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import com.arkivanov.essenty.backhandler.BackEvent
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.navigation.animations.ContentAnimatorCreator

/**
 * Base for the content animator scope implementations. Describes the bare minimum
 * needed. When implementing - keep in mind:
 * - elements, appearing and disappearing from the stack, update the state and trigger animations
 *
 * - [onBackGesture] represents the user's actions and should not be used to manipulate the stack
 *
 * - [indexFromTop] represents the index of the item from the top of the stack, with
 * 0 being the top. Negative numbers represent an item not existing in the stack while being
 * animated. If several items are being removed and all are animated at the same time - 
 * [indexFromTop] will be represent the order of the items being removed, -1 being
 * the latest item that has been removed. Yes, the number can be less than -1.
 *
 * [animationProgressForScope] is a value that is used to provide [AnimatedVisibilityScope]'s
 * to the content for things like modifiers that depend on [SharedTransitionScope].
 * The resulting value provided to the [AnimatedVisibilityScope] is constrained to a
 * range between 0f and 1f. It's expected to mirror [indexFromTop], meaning it must
 * be -1 when the item is outside of the stack, 0 when at the top of the stack, and 1 when
 * at the back of the stack. It's also expected to depend on back gestures.
 *
 * Note: when several animations with different keys are used for a single item -
 * the first scope is used to provide [animationProgressForScope]. Refer to
 * [ContentAnimatorCreator] and [contentAnimator] for more information.
 */
interface ContentAnimator {
    val indexFromTop: Int
    val index: Int
    val animationStatus: AnimationStatus
    val animationProgressForScope: Float

    suspend fun onBackGestureStarted(backEvent: BackEvent)
    suspend fun onBackGestureProgressed(backEvent: BackEvent)
    suspend fun onBackGestureCancelled()
    suspend fun onBackGestureConfirmed()
    suspend fun update(newIndex: Int, newIndexFromTop: Int)
}