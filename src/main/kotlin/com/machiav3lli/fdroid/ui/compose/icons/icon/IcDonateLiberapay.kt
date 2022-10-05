package com.machiav3lli.fdroid.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Icon

val Icon.IcDonateLiberapay: ImageVector
    get() {
        if (_icDonateLiberapay != null) {
            return _icDonateLiberapay!!
        }
        _icDonateLiberapay = Builder(
            name = "IcDonateLiberapay",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(11.393f, 1.347f)
                lineTo(8.018f, 1.869f)
                lineTo(5.253f, 13.43f)
                curveToRelative(-0.16f, 0.682f, -0.244f, 1.325f, -0.251f, 1.927f)
                curveToRelative(-0.007f, 0.604f, 0.116f, 1.136f, 0.37f, 1.6f)
                curveToRelative(0.254f, 0.465f, 0.675f, 0.831f, 1.262f, 1.1f)
                curveToRelative(0.588f, 0.268f, 1.397f, 0.402f, 2.428f, 0.402f)
                verticalLineToRelative(-0.002f)
                lineTo(9.716f, 15.781f)
                curveTo(9.339f, 15.752f, 9.045f, 15.687f, 8.834f, 15.585f)
                curveTo(8.624f, 15.484f, 8.475f, 15.35f, 8.388f, 15.183f)
                curveTo(8.301f, 15.016f, 8.261f, 14.823f, 8.269f, 14.605f)
                curveToRelative(0.007f, -0.217f, 0.04f, -0.457f, 0.098f, -0.718f)
                close()
                moveToRelative(5.201f, 5.184f)
                curveToRelative(-0.871f, 0.0f, -1.68f, 0.069f, -2.428f, 0.207f)
                curveToRelative(-0.747f, 0.138f, -1.412f, 0.294f, -1.992f, 0.468f)
                lineTo(8.559f, 22.27f)
                horizontalLineTo(11.782f)
                lineToRelative(0.98f, -3.941f)
                curveToRelative(0.493f, 0.087f, 0.987f, 0.13f, 1.48f, 0.13f)
                curveToRelative(1.015f, 0.0f, 1.956f, -0.178f, 2.82f, -0.534f)
                curveToRelative(0.864f, -0.355f, 1.603f, -0.851f, 2.22f, -1.491f)
                curveToRelative(0.617f, -0.639f, 1.099f, -1.397f, 1.448f, -2.276f)
                curveToRelative(0.348f, -0.878f, 0.522f, -1.846f, 0.523f, -2.905f)
                curveToRelative(0.0f, 0.0f, 0.0f, -0.001f, 0.0f, -0.001f)
                curveToRelative(0.0f, 0.0f, 0.0f, -0.001f, 0.0f, -0.001f)
                curveTo(21.252f, 10.601f, 21.162f, 9.987f, 20.98f, 9.415f)
                curveTo(20.799f, 8.842f, 20.519f, 8.342f, 20.142f, 7.913f)
                curveTo(19.765f, 7.485f, 19.283f, 7.147f, 18.695f, 6.901f)
                curveTo(18.107f, 6.654f, 17.407f, 6.53f, 16.594f, 6.53f)
                close()
                moveToRelative(-0.414f, 2.722f)
                curveToRelative(0.682f, 0.0f, 1.161f, 0.218f, 1.437f, 0.653f)
                curveToRelative(0.275f, 0.436f, 0.413f, 0.966f, 0.413f, 1.59f)
                curveToRelative(0.0f, 0.639f, -0.091f, 1.223f, -0.272f, 1.752f)
                curveToRelative(-0.182f, 0.53f, -0.436f, 0.984f, -0.762f, 1.361f)
                curveToRelative(-0.327f, 0.378f, -0.723f, 0.671f, -1.187f, 0.881f)
                curveToRelative(-0.465f, 0.211f, -0.98f, 0.316f, -1.546f, 0.316f)
                curveToRelative(-0.363f, 0.0f, -0.667f, -0.029f, -0.914f, -0.087f)
                lineToRelative(1.523f, -6.335f)
                curveToRelative(0.406f, -0.087f, 0.842f, -0.131f, 1.307f, -0.131f)
                close()
            }
        }
            .build()
        return _icDonateLiberapay!!
    }

private var _icDonateLiberapay: ImageVector? = null
