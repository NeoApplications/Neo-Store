package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Scales: ImageVector
    get() {
        if (_Scales != null) {
            return _Scales!!
        }
        _Scales = ImageVector.Builder(
            name = "Scales",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(239.43f, 133f)
                lineToRelative(-32f, -80f)
                horizontalLineToRelative(0f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -9.16f,
                    -4.84f
                )
                lineTo(136f, 62f)
                lineTo(136f, 40f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 0f)
                lineTo(120f, 65.58f)
                lineTo(54.26f, 80.19f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48.57f, 85f)
                horizontalLineToRelative(0f)
                verticalLineToRelative(0.06f)
                lineTo(16.57f, 165f)
                arcToRelative(
                    7.92f,
                    7.92f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.57f,
                    3f
                )
                curveToRelative(0f, 23.31f, 24.54f, 32f, 40f, 32f)
                reflectiveCurveToRelative(40f, -8.69f, 40f, -32f)
                arcToRelative(
                    7.92f,
                    7.92f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.57f,
                    -3f
                )
                lineTo(66.92f, 93.77f)
                lineTo(120f, 82f)
                lineTo(120f, 208f)
                lineTo(104f, 208f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 16f)
                horizontalLineToRelative(48f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -16f)
                lineTo(136f, 208f)
                lineTo(136f, 78.42f)
                lineTo(187f, 67.1f)
                lineTo(160.57f, 133f)
                arcToRelative(
                    7.92f,
                    7.92f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.57f,
                    3f
                )
                curveToRelative(0f, 23.31f, 24.54f, 32f, 40f, 32f)
                reflectiveCurveToRelative(40f, -8.69f, 40f, -32f)
                arcTo(
                    7.92f,
                    7.92f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    239.43f,
                    133f
                )
                close()
                moveTo(56f, 184f)
                curveToRelative(-7.53f, 0f, -22.76f, -3.61f, -23.93f, -14.64f)
                lineTo(56f, 109.54f)
                lineToRelative(23.93f, 59.82f)
                curveTo(78.76f, 180.39f, 63.53f, 184f, 56f, 184f)
                close()
                moveTo(200f, 152f)
                curveToRelative(-7.53f, 0f, -22.76f, -3.61f, -23.93f, -14.64f)
                lineTo(200f, 77.54f)
                lineToRelative(23.93f, 59.82f)
                curveTo(222.76f, 148.39f, 207.53f, 152f, 200f, 152f)
                close()
            }
        }.build()

        return _Scales!!
    }

@Suppress("ObjectPropertyName")
private var _Scales: ImageVector? = null
