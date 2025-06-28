package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.ShieldCheck: ImageVector
    get() {
        if (_ShieldCheck != null) {
            return _ShieldCheck!!
        }
        _ShieldCheck = ImageVector.Builder(
            name = "ShieldCheck",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(208f, 40f)
                lineTo(48f, 40f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 56f)
                verticalLineToRelative(56f)
                curveToRelative(0f, 52.72f, 25.52f, 84.67f, 46.93f, 102.19f)
                curveToRelative(23.06f, 18.86f, 46f, 25.26f, 47f, 25.53f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.2f, 0f)
                curveToRelative(1f, -0.27f, 23.91f, -6.67f, 47f, -25.53f)
                curveTo(198.48f, 196.67f, 224f, 164.72f, 224f, 112f)
                lineTo(224f, 56f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 208f, 40f)
                close()
                moveTo(208f, 112f)
                curveToRelative(0f, 37.07f, -13.66f, 67.16f, -40.6f, 89.42f)
                arcTo(
                    129.3f,
                    129.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    128f,
                    223.62f
                )
                arcToRelative(
                    128.25f,
                    128.25f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -38.92f,
                    -21.81f
                )
                curveTo(61.82f, 179.51f, 48f, 149.3f, 48f, 112f)
                lineToRelative(0f, -56f)
                lineToRelative(160f, 0f)
                close()
                moveTo(82.34f, 141.66f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    11.32f,
                    -11.32f
                )
                lineTo(112f, 148.69f)
                lineToRelative(50.34f, -50.35f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    11.32f,
                    11.32f
                )
                lineToRelative(-56f, 56f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.32f, 0f)
                close()
            }
        }.build()

        return _ShieldCheck!!
    }

@Suppress("ObjectPropertyName")
private var _ShieldCheck: ImageVector? = null