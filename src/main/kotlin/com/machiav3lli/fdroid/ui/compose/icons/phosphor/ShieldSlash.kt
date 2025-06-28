package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.ShieldSlash: ImageVector
    get() {
        if (_ShieldSlash != null) {
            return _ShieldSlash!!
        }
        _ShieldSlash = ImageVector.Builder(
            name = "ShieldSlash",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(53.92f, 34.62f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 40.26f, 42f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 56f)
                verticalLineToRelative(56f)
                curveToRelative(0f, 52.72f, 25.52f, 84.67f, 46.93f, 102.19f)
                curveToRelative(23.06f, 18.86f, 46f, 25.27f, 47f, 25.53f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.2f, 0f)
                curveToRelative(1.36f, -0.37f, 31.27f, -8.78f, 57.09f, -34.72f)
                lineToRelative(14.89f, 16.38f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    11.84f,
                    -10.76f
                )
                close()
                moveTo(127.99f, 223.62f)
                arcToRelative(
                    128.48f,
                    128.48f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -38.92f,
                    -21.81f
                )
                curveTo(61.82f, 179.51f, 48f, 149.3f, 48f, 112f)
                lineToRelative(0f, -56f)
                horizontalLineToRelative(3.71f)
                lineTo(176.41f, 193.15f)
                arcTo(
                    129.26f,
                    129.26f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    128f,
                    223.62f
                )
                close()
                moveTo(224f, 56f)
                verticalLineToRelative(56f)
                curveToRelative(0f, 20.58f, -3.89f, 39.61f, -11.56f, 56.59f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 197.86f, 162f)
                curveToRelative(6.73f, -14.89f, 10.14f, -31.71f, 10.14f, -50f)
                lineTo(208f, 56f)
                lineTo(98.52f, 56f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -16f)
                lineTo(208f, 40f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 56f)
                close()
            }
        }.build()

        return _ShieldSlash!!
    }

@Suppress("ObjectPropertyName")
private var _ShieldSlash: ImageVector? = null