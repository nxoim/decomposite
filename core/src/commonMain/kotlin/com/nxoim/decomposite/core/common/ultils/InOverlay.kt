package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Composable
import com.nxoim.decomposite.core.common.navigation.LocalNavigationRoot
import com.nxoim.decomposite.core.common.navigation.NavigationRootProvider

/**
 * Displays content in the overlay provided [NavigationRootProvider]
 */
@Composable
fun InOverlay(content: @Composable () -> Unit) {
	LocalNavigationRoot.current.overlay { content() }
}