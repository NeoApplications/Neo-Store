package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

@Composable
inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: @Composable Modifier.() -> Modifier
): Modifier =
    if (condition) factory() else this

fun Modifier.vertical() =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }
