package com.nxoim.decomposite.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
internal actual fun SampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        LightColorScheme,
        typography = Typography,
        content = content
    )
}