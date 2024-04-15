package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size

data class AppConfiguration(
    val maxSizePixels: Size
)

val LocalConfiguration = staticCompositionLocalOf<AppConfiguration> {
    error("No AppConfiguration provided")
}