package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Describes the type of displayed content. Can be retrieved using [LocalContentType]
 */
enum class ContentType {
    Contained, Overlay
}

val LocalContentType = staticCompositionLocalOf<ContentType> {
    error("No ContentType provided")
}