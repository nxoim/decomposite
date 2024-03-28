package com.nxoim.decomposite.core.common.ultils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

@Stable
fun Modifier.noRippleClickable(onClick: () -> Unit = {}) = this.clickable(
    indication = null,
    interactionSource = MutableInteractionSource(),
    onClick = onClick
)