package com.nxoim.decomposite.core.common.navigation.animations.scopes

import com.nxoim.decomposite.core.common.navigation.animations.AnimationStatus
import com.nxoim.decomposite.core.common.ultils.BackGestureEvent

interface ContentAnimatorScope {
    val indexFromTop: Int
    val index: Int
    val animationStatus: AnimationStatus

    suspend fun onBackGesture(backGesture: BackGestureEvent): Any
    suspend fun update(newIndex: Int, newIndexFromTop: Int, animate: Boolean = true)
}

