package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Metronome: ImageVector
    get() {
        if (_Metronome != null) {
            return _Metronome!!
        }
        _Metronome = ImageVector.Builder(
            name = "Metronome",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(187.14f, 114.84f)
                lineToRelative(26.78f, -29.46f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.84f,
                    -10.76f
                )
                lineToRelative(-20.55f, 22.6f)
                lineToRelative(-17.2f, -54.07f)
                arcTo(
                    15.94f,
                    15.94f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    149.08f,
                    32f
                )
                horizontalLineTo(106.91f)
                arcTo(
                    15.94f,
                    15.94f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    91.66f,
                    43.15f
                )
                lineToRelative(-50.91f, 160f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 224f)
                horizontalLineTo(200f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    15.25f,
                    -20.85f
                )
                close()
                moveTo(184.72f, 160f)
                horizontalLineTo(146.08f)
                lineToRelative(28.62f, -31.48f)
                close()
                moveTo(106.91f, 48f)
                horizontalLineToRelative(42.17f)
                lineToRelative(20f, 62.9f)
                lineTo(124.46f, 160f)
                horizontalLineTo(71.27f)
                close()
                moveTo(56f, 208f)
                lineToRelative(10.18f, -32f)
                horizontalLineTo(189.81f)
                lineTo(200f, 208f)
                close()
            }
        }.build()

        return _Metronome!!
    }

@Suppress("ObjectPropertyName")
private var _Metronome: ImageVector? = null