package com.number869.decomposite.core.common.ultils

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext

fun ComponentContext.assemble() = DefaultComponentContext(
    this.lifecycle, this.stateKeeper, this.instanceKeeper, this.backHandler
)