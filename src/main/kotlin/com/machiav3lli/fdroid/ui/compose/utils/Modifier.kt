package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.data.content.Preferences

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

fun Modifier.horizontal() =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }

fun Modifier.blockBorderTop(altStyle: Boolean = !Preferences[Preferences.Key.AltBlockLayout]) =
    composed {
        this
            .padding(2.dp)
            .clip(BlockTopShape)
            .addIfElse(altStyle,
                factory = {
                    border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = BlockTopShape,
                    )
                },
                elseFactory = {
                    background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                }
            )
    }

fun Modifier.blockBorderBottom(altStyle: Boolean = !Preferences[Preferences.Key.AltBlockLayout]) =
    composed {
        this
            .padding(2.dp)
            .clip(BlockBottomShape)
            .addIfElse(altStyle,
                factory = {
                    border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = BlockBottomShape,
                    )
                },
                elseFactory = {
                    background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                }
            )
    }

fun Modifier.blockShadow(altStyle: Boolean = !Preferences[Preferences.Key.AltBlockLayout]) =
    composed {
        this
            .addIfElse(altStyle,
                factory = {
                    border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                },
                elseFactory = {
                    shadow(elevation = 1.dp, shape = MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                }
            )
    }

val BlockTopShape
    @Composable @ReadOnlyComposable get() = RoundedCornerShape(
        topStart = MaterialTheme.shapes.extraLarge.topStart,
        topEnd = MaterialTheme.shapes.extraLarge.topEnd,
        bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd,
        bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
    )

val BlockBottomShape
    @Composable @ReadOnlyComposable get() = RoundedCornerShape(
        topStart = MaterialTheme.shapes.extraSmall.topStart,
        topEnd = MaterialTheme.shapes.extraSmall.topEnd,
        bottomEnd = MaterialTheme.shapes.extraLarge.bottomEnd,
        bottomStart = MaterialTheme.shapes.extraLarge.bottomStart,
    )
