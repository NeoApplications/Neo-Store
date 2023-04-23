package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp

@Composable
inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: @Composable Modifier.() -> Modifier,
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

@Composable
fun Modifier.blockBorder() = this
    .clip(MaterialTheme.shapes.extraLarge)
    .border(
        2.dp,
        MaterialTheme.colorScheme.outlineVariant,
        MaterialTheme.shapes.extraLarge,
    )
