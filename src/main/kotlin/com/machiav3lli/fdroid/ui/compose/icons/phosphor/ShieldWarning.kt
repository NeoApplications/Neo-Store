package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.ShieldWarning: ImageVector
    get() {
        if (_ShieldWarning != null) {
            return _ShieldWarning!!
        }
        _ShieldWarning = ImageVector.Builder(
            name = "ShieldWarning",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(120f, 136f)
                lineTo(120f, 96f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 0f)
                verticalLineToRelative(40f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 0f)
                close()
                moveTo(128f, 184f)
                arcToRelative(
                    12f,
                    12f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -12f,
                    -12f
                )
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 128f, 184f)
                close()
                moveTo(224f, 56f)
                verticalLineToRelative(56f)
                curveToRelative(0f, 52.72f, -25.52f, 84.67f, -46.93f, 102.19f)
                curveToRelative(-23.06f, 18.86f, -46f, 25.27f, -47f, 25.53f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.2f, 0f)
                curveToRelative(-1f, -0.26f, -23.91f, -6.67f, -47f, -25.53f)
                curveTo(57.52f, 196.67f, 32f, 164.72f, 32f, 112f)
                lineTo(32f, 56f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 40f)
                lineTo(208f, 40f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 56f)
                close()
                moveTo(208f, 56f)
                lineTo(48f, 56f)
                lineToRelative(0f, 56f)
                curveToRelative(0f, 37.3f, 13.82f, 67.51f, 41.07f, 89.81f)
                arcTo(
                    128.25f,
                    128.25f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    128f,
                    223.62f
                )
                arcToRelative(
                    129.3f,
                    129.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    39.41f,
                    -22.2f
                )
                curveTo(194.34f, 179.16f, 208f, 149.07f, 208f, 112f)
                close()
            }
        }.build()

        return _ShieldWarning!!
    }

@Suppress("ObjectPropertyName")
private var _ShieldWarning: ImageVector? = null
