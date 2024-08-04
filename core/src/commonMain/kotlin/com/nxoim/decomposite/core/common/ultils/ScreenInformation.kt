package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Immutable

@Immutable
data class ScreenInformation(
    val widthPx: Int,
    val heightPx: Int,
    val screenShape: ScreenShape
)

@Immutable
data class ScreenShapeCorners(
    val topLeftPx: Int,
    val topRightPx: Int,
    val bottomLeftPx: Int,
    val bottomRightPx: Int,
)

@Immutable
data class ScreenShape(
    val path: androidx.compose.ui.graphics.Path?,
    val corners: ScreenShapeCorners?
)

