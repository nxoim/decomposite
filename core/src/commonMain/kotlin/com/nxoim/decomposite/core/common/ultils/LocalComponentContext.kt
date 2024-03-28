package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.staticCompositionLocalOf
import com.arkivanov.decompose.ComponentContext

val LocalComponentContext = staticCompositionLocalOf<ComponentContext> {
    // Provide a default value for the composition local if needed
    error("No ComponentContext provided")
}