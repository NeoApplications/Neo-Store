package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.content.Preferences

@Composable
inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: @Composable Modifier.() -> Modifier,
): Modifier =
    if (condition) this.factory() else this

@Composable
inline fun Modifier.addIfElse(
    condition: Boolean,
    crossinline factory: @Composable Modifier.() -> Modifier,
    crossinline elseFactory: @Composable Modifier.() -> Modifier,
): Modifier =
    if (condition) this.factory() else this.elseFactory()

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

fun Modifier.blockBorder(altStyle: Boolean = Preferences[Preferences.Key.AltBlockLayout]) =
    composed {
        this
            .clip(MaterialTheme.shapes.extraLarge)
            .addIfElse(altStyle,
                factory = {
                    border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                },
                elseFactory = {
                    background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                }
            )
    }

fun Modifier.blockShadow(altStyle: Boolean = Preferences[Preferences.Key.AltBlockLayout]) =
    composed {
        this
            .addIfElse(altStyle,
                factory = {
                    border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                },
                elseFactory = {
                    shadow(elevation = 1.dp, shape = MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                }
            )
    }
