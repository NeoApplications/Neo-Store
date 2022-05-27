package com.saggitt.omega.compose.navigation

import androidx.compose.runtime.*

@Composable
fun <T> resultSender(): (T) -> Unit {
    val navController = LocalNavController.current
    return { item: T ->
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("result", item)
        navController.popBackStack()
        Unit
    }
}

@Composable
fun <T> OnResult(callback: (result: T) -> Unit) {
    val currentCallback = rememberUpdatedState(callback)
    var fired by remember { mutableStateOf(false) }

    val handle = LocalNavController.current.currentBackStackEntry?.savedStateHandle
    val result = handle?.getLiveData<T>("result")

    SideEffect {
        result?.value?.let {
            if (fired) return@let
            fired = true
            currentCallback.value(it)
            handle.remove<T>("result")
            Unit
        }
    }
}