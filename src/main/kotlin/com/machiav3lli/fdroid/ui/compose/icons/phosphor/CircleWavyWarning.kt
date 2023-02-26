package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.CircleWavyWarning: ImageVector
    get() {
        if (_circle_wavy_warning != null) {
            return _circle_wavy_warning!!
        }
        _circle_wavy_warning = Builder(
            name = "Circle-wavy-warning",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(225.9f, 102.8f)
                curveToRelative(-3.8f, -3.9f, -7.7f, -8.0f, -9.2f, -11.5f)
                reflectiveCurveToRelative(-1.4f, -8.7f, -1.5f, -14.0f)
                curveToRelative(-0.1f, -9.7f, -0.3f, -20.8f, -8.0f, -28.5f)
                reflectiveCurveToRelative(-18.8f, -7.9f, -28.5f, -8.0f)
                curveToRelative(-5.3f, -0.1f, -10.7f, -0.2f, -14.0f, -1.5f)
                reflectiveCurveToRelative(-7.6f, -5.4f, -11.5f, -9.2f)
                curveTo(146.3f, 23.5f, 138.4f, 16.0f, 128.0f, 16.0f)
                reflectiveCurveToRelative(-18.3f, 7.5f, -25.2f, 14.1f)
                curveToRelative(-3.9f, 3.8f, -8.0f, 7.7f, -11.5f, 9.2f)
                reflectiveCurveToRelative(-8.7f, 1.4f, -14.0f, 1.5f)
                curveToRelative(-9.7f, 0.1f, -20.8f, 0.3f, -28.5f, 8.0f)
                reflectiveCurveToRelative(-7.9f, 18.8f, -8.0f, 28.5f)
                curveToRelative(-0.1f, 5.3f, -0.2f, 10.7f, -1.5f, 14.0f)
                reflectiveCurveToRelative(-5.4f, 7.6f, -9.2f, 11.5f)
                curveTo(23.5f, 109.7f, 16.0f, 117.6f, 16.0f, 128.0f)
                reflectiveCurveToRelative(7.5f, 18.3f, 14.1f, 25.2f)
                curveToRelative(3.8f, 3.9f, 7.7f, 8.0f, 9.2f, 11.5f)
                reflectiveCurveToRelative(1.4f, 8.7f, 1.5f, 14.0f)
                curveToRelative(0.1f, 9.7f, 0.3f, 20.8f, 8.0f, 28.5f)
                reflectiveCurveToRelative(18.8f, 7.9f, 28.5f, 8.0f)
                curveToRelative(5.3f, 0.1f, 10.7f, 0.2f, 14.0f, 1.5f)
                reflectiveCurveToRelative(7.6f, 5.4f, 11.5f, 9.2f)
                curveToRelative(6.9f, 6.6f, 14.8f, 14.1f, 25.2f, 14.1f)
                reflectiveCurveToRelative(18.3f, -7.5f, 25.2f, -14.1f)
                curveToRelative(3.9f, -3.8f, 8.0f, -7.7f, 11.5f, -9.2f)
                reflectiveCurveToRelative(8.7f, -1.4f, 14.0f, -1.5f)
                curveToRelative(9.7f, -0.1f, 20.8f, -0.3f, 28.5f, -8.0f)
                reflectiveCurveToRelative(7.9f, -18.8f, 8.0f, -28.5f)
                curveToRelative(0.1f, -5.3f, 0.2f, -10.7f, 1.5f, -14.0f)
                reflectiveCurveToRelative(5.4f, -7.6f, 9.2f, -11.5f)
                curveToRelative(6.6f, -6.9f, 14.1f, -14.8f, 14.1f, -25.2f)
                reflectiveCurveTo(232.5f, 109.7f, 225.9f, 102.8f)
                close()
                moveTo(214.3f, 142.1f)
                curveToRelative(-4.8f, 5.0f, -9.7f, 10.2f, -12.4f, 16.5f)
                reflectiveCurveToRelative(-2.6f, 13.1f, -2.7f, 19.8f)
                reflectiveCurveToRelative(-0.2f, 14.4f, -3.3f, 17.5f)
                reflectiveCurveToRelative(-10.4f, 3.2f, -17.5f, 3.3f)
                reflectiveCurveToRelative(-13.7f, 0.2f, -19.8f, 2.7f)
                reflectiveCurveToRelative(-11.5f, 7.6f, -16.5f, 12.4f)
                reflectiveCurveTo(132.0f, 224.0f, 128.0f, 224.0f)
                reflectiveCurveToRelative(-9.1f, -4.9f, -14.1f, -9.7f)
                reflectiveCurveToRelative(-10.2f, -9.7f, -16.5f, -12.4f)
                reflectiveCurveToRelative(-13.1f, -2.6f, -19.8f, -2.7f)
                reflectiveCurveToRelative(-14.4f, -0.2f, -17.5f, -3.3f)
                reflectiveCurveToRelative(-3.2f, -10.4f, -3.3f, -17.5f)
                reflectiveCurveToRelative(-0.2f, -13.7f, -2.7f, -19.8f)
                reflectiveCurveToRelative(-7.6f, -11.5f, -12.4f, -16.5f)
                reflectiveCurveTo(32.0f, 132.0f, 32.0f, 128.0f)
                reflectiveCurveToRelative(4.9f, -9.1f, 9.7f, -14.1f)
                reflectiveCurveToRelative(9.7f, -10.2f, 12.4f, -16.5f)
                reflectiveCurveToRelative(2.6f, -13.1f, 2.7f, -19.8f)
                reflectiveCurveToRelative(0.2f, -14.4f, 3.3f, -17.5f)
                reflectiveCurveToRelative(10.4f, -3.2f, 17.5f, -3.3f)
                reflectiveCurveToRelative(13.7f, -0.2f, 19.8f, -2.7f)
                reflectiveCurveToRelative(11.5f, -7.6f, 16.5f, -12.4f)
                reflectiveCurveTo(124.0f, 32.0f, 128.0f, 32.0f)
                reflectiveCurveToRelative(9.1f, 4.9f, 14.1f, 9.7f)
                reflectiveCurveToRelative(10.2f, 9.7f, 16.5f, 12.4f)
                reflectiveCurveToRelative(13.1f, 2.6f, 19.8f, 2.7f)
                reflectiveCurveToRelative(14.4f, 0.2f, 17.5f, 3.3f)
                reflectiveCurveToRelative(3.2f, 10.4f, 3.3f, 17.5f)
                reflectiveCurveToRelative(0.2f, 13.7f, 2.7f, 19.8f)
                reflectiveCurveToRelative(7.6f, 11.5f, 12.4f, 16.5f)
                reflectiveCurveTo(224.0f, 124.0f, 224.0f, 128.0f)
                reflectiveCurveTo(219.1f, 137.1f, 214.3f, 142.1f)
                close()
                moveTo(120.0f, 136.0f)
                lineTo(120.0f, 80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                close()
                moveTo(140.0f, 172.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 140.0f, 172.0f)
                close()
            }
        }
            .build()
        return _circle_wavy_warning!!
    }

private var _circle_wavy_warning: ImageVector? = null
