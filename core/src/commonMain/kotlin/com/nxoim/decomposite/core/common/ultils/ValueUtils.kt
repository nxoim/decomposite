package com.nxoim.decomposite.core.common.ultils

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

@Stable
fun <T : Any> Value<T>.asState(): State<T> {
    val state = mutableStateOf(this.value)

    this.subscribe { state.value = it }

    return state
}

@Stable
fun <C : Any, T : Any> Value<ChildStack<C, T>>.activeAsState(): State<C> {
    val state = mutableStateOf(this.value.active.configuration)

    this.subscribe { state.value = it.active.configuration }

    return state
}