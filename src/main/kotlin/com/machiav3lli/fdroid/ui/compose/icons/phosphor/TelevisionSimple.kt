package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.TelevisionSimple: ImageVector
    get() {
        if (_TelevisionSimple != null) {
            return _TelevisionSimple!!
        }
        _TelevisionSimple = ImageVector.Builder(
            name = "TelevisionSimple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(216f, 64f)
                lineTo(147.31f, 64f)
                lineToRelative(34.35f, -34.34f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -11.32f,
                    -11.32f
                )
                lineTo(128f, 60.69f)
                lineTo(85.66f, 18.34f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 74.34f, 29.66f)
                lineTo(108.69f, 64f)
                lineTo(40f, 64f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 80f)
                lineTo(24f, 200f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(216f, 216f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -16f
                )
                lineTo(232f, 80f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 216f, 64f)
                close()
                moveTo(216f, 200f)
                lineTo(40f, 200f)
                lineTo(40f, 80f)
                lineTo(216f, 80f)
                lineTo(216f, 200f)
                close()
            }
        }.build()

        return _TelevisionSimple!!
    }

@Suppress("ObjectPropertyName")
private var _TelevisionSimple: ImageVector? = null