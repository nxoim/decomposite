package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.staticCompositionLocalOf
import com.arkivanov.essenty.backhandler.BackDispatcher

val LocalBackDispatcher = staticCompositionLocalOf<BackDispatcher> {
    error("No BackDispatcher provided")
}