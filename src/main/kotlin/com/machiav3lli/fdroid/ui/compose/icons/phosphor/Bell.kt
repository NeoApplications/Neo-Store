package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Bell: ImageVector
    get() {
        if (_bell != null) {
            return _bell!!
        }
        _bell = ImageVector.Builder(
            name = "Bell",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(221.8f, 175.94f)
                curveTo(216.25f, 166.38f, 208f, 139.33f, 208f, 104f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = true, isPositiveArc = false, -160f, 0f)
                curveToRelative(0f, 35.34f, -8.26f, 62.38f, -13.81f, 71.94f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 200f)
                horizontalLineTo(88.81f)
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    78.38f,
                    0f
                )
                horizontalLineTo(208f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    13.8f,
                    -24.06f
                )
                close()
                moveTo(128f, 216f)
                arcToRelative(
                    24f,
                    24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -22.62f,
                    -16f
                )
                horizontalLineToRelative(45.24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 128f, 216f)
                close()
                moveTo(48f, 184f)
                curveToRelative(7.7f, -13.24f, 16f, -43.92f, 16f, -80f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 128f, 0f)
                curveToRelative(0f, 36.05f, 8.28f, 66.73f, 16f, 80f)
                close()
            }
        }.build()

        return _bell!!
    }

@Suppress("ObjectPropertyName")
private var _bell: ImageVector? = null
