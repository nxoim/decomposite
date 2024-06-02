package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.staticCompositionLocalOf
import com.arkivanov.decompose.ComponentContext

val LocalComponentContext = staticCompositionLocalOf<ComponentContext> {
    error("No ComponentContext provided")
}