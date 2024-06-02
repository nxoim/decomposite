package com.nxoim.decomposite.core.common.navigation.animations.scopes

import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent

/**
 * Base for the content animator scope implementations. Describes the bare minimum
 * needed. When implementing - keep in mind:
 * - elements, appearing and disappearing from the stack, update the state and trigger animations
 * - [onBackGesture] represents the user's actions and should not be used to manipulate the stack
 * - in [update], when animating exit - let the animation code (be it animateTo or animate) block
 * the thread so the content removal happens only after the animation has ended
 */
interface ContentAnimatorScope {
    val indexFromTop: Int
    val index: Int
    val animationStatus: AnimationStatus

    suspend fun onBackGesture(backGesture: BackGestureEvent): Any
    suspend fun update(newIndex: Int, newIndexFromTop: Int, animate: Boolean = true)
}

