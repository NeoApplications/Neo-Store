package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.TwoCircle: ImageVector
    get() {
        if (_twoCircle != null) {
            return _twoCircle!!
        }
        _twoCircle = ImageVector.Builder(
            name = "TwoCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(128f, 24f)
                arcTo(104f, 104f, 0f, isMoreThanHalf = true, isPositiveArc = false, 232f, 128f)
                arcTo(
                    104.11f,
                    104.11f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    128f,
                    24f
                )
                close()
                moveTo(128f, 216f)
                arcToRelative(88f, 88f, 0f, isMoreThanHalf = true, isPositiveArc = true, 88f, -88f)
                arcTo(88.1f, 88.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 128f, 216f)
                close()
                moveTo(153.56f, 123.26f)
                lineTo(120f, 168f)
                horizontalLineToRelative(32f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 16f)
                lineTo(104f, 184f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -6.4f,
                    -12.8f
                )
                lineToRelative(43.17f, -57.56f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -27.86f,
                    -15f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -15.09f,
                    -5.34f
                )
                arcToRelative(
                    32f,
                    32f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    55.74f,
                    29.93f
                )
                close()
            }
        }.build()

        return _twoCircle!!
    }

@Suppress("ObjectPropertyName")
private var _twoCircle: ImageVector? = null
