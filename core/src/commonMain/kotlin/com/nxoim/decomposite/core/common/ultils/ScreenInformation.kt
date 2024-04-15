package com.nxoim.decomposite.core.common.ultils

data class ScreenInformation(
    val widthPx: Int,
    val heightPx: Int,
    val screenShape: ScreenShape
)

data class ScreenShapeCorners(
    val topLeftPx: Int,
    val topRightPx: Int,
    val bottomLeftPx: Int,
    val bottomRightPx: Int,
)

data class ScreenShape(
    val path: androidx.compose.ui.graphics.Path?,
    val corners: ScreenShapeCorners?
)

