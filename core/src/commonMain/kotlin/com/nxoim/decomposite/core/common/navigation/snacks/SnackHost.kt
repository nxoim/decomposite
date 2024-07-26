package com.nxoim.decomposite.core.common.navigation.snacks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nxoim.decomposite.core.common.navigation.animations.StackAnimator
import com.nxoim.decomposite.core.common.navigation.animations.emptyAnimation
import com.nxoim.decomposite.core.common.navigation.animations.rememberStackAnimatorScope
import com.nxoim.decomposite.core.common.ultils.LocalComponentContext
import com.nxoim.decomposite.core.common.ultils.OnDestinationDisposeEffect

/**
 * Hosts the snack content
 */
@Composable
fun SnackHost(snackController: SnackController = snackController()) {
    // snacks don't need to be aware of gestures
    StackAnimator(
        stackState = snackController.snackStack.subscribeAsState(),
        stackAnimatorScope = rememberStackAnimatorScope("snack content"),
        onBackstackChange = {},
        animations = {
            snackController.animationsForDestinations[currentChild] ?: emptyAnimation()
        },
        content = {
            CompositionLocalProvider(
                LocalComponentContext provides it.instance.componentContext,
                content = {
                    snackController.contentOfSnacks[it.configuration]?.invoke()

                    OnDestinationDisposeEffect("snack of $it", true) {
                        snackController.removeSnackContents(it.configuration)
                    }
                }
            )
        }
    )
}