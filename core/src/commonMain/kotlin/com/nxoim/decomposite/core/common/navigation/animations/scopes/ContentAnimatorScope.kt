package com.nxoim.decomposite.core.common.navigation.animations.scopes

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.State
import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent

/**
 * Base for the content animator scope implementations. Describes the bare minimum
 * needed. When implementing - keep in mind:
 * - elements, appearing and disappearing from the stack, update the state and trigger animations
 * - [onBackGesture] represents the user's actions and should not be used to manipulate the stack
 * - in [update], when animating exit - let the animation code (be it animateTo or animate) block
 * the thread so the content removal happens only after the animation has ended.
 *
 * [animationProgressForScope] describes the current progress of an animation
 * and is used for providing the [AnimatedVisibilityScope] to the content, for
 * things like modifiers that depend on [SharedTransitionScope].
 * It must mirror [indexFromTop], meaning it must be -1 when the item is outside the stack,
 * 0 when at the top of the stack, and 1 when at the back of the stack.
 *
 * Note: when several animations with different specs are used for a single item -
 * the first scope is used to provide [animationProgressForScope].
 */
interface ContentAnimatorScope {
    val indexFromTop: Int
    val index: Int
    val animationStatus: AnimationStatus
    val animationProgressForScope: State<Float>

    suspend fun onBackGesture(backGesture: BackGestureEvent): Any
    suspend fun update(newIndex: Int, newIndexFromTop: Int, animate: Boolean = true)
}