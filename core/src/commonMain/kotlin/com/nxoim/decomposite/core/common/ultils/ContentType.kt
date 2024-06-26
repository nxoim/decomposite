package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.staticCompositionLocalOf

enum class ContentType {
    Contained, Overlay
}

val LocalContentType = staticCompositionLocalOf<ContentType> {
    error("No ContentType provided")
}