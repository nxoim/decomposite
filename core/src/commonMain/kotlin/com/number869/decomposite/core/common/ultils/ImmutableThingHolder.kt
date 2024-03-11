package com.number869.decomposite.core.common.ultils

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

@JvmInline
@Immutable
value class ImmutableThingHolder<T>(val thing: T)