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

val Icon.IcDonateFlattr: ImageVector
    get() {
        if (_icDonateFlattr != null) {
            return _icDonateFlattr!!
        }
        _icDonateFlattr = Builder(
            name = "IcDonateFlattr",
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
                moveTo(9.461f, 3.0f)
                curveTo(5.183f, 3.0f, 3.0f, 5.464f, 3.0f, 10.064f)
                verticalLineToRelative(3.213f)
                verticalLineToRelative(6.438f)
                lineTo(7.19f, 15.52f)
                verticalLineToRelative(-4.902f)
                curveToRelative(0.0f, -1.906f, 0.505f, -3.118f, 2.199f, -3.391f)
                curveToRelative(0.592f, -0.116f, 1.824f, -0.075f, 2.607f, -0.075f)
                verticalLineToRelative(2.911f)
                curveToRelative(0.0f, 0.027f, 0.004f, 0.074f, 0.01f, 0.098f)
                curveToRelative(0.033f, 0.118f, 0.139f, 0.204f, 0.266f, 0.204f)
                curveToRelative(0.071f, 0.0f, 0.138f, -0.037f, 0.207f, -0.105f)
                lineToRelative(7.262f, -7.259f)
                lineToRelative(-4.875f, -0.001f)
                close()
                moveTo(21.0f, 4.285f)
                lineTo(16.81f, 8.48f)
                verticalLineToRelative(4.902f)
                curveToRelative(0.0f, 1.906f, -0.505f, 3.118f, -2.199f, 3.391f)
                curveToRelative(-0.592f, 0.116f, -1.824f, 0.075f, -2.607f, 0.075f)
                verticalLineToRelative(-2.911f)
                curveToRelative(0.0f, -0.026f, -0.004f, -0.074f, -0.01f, -0.098f)
                curveToRelative(-0.033f, -0.118f, -0.139f, -0.204f, -0.266f, -0.204f)
                curveToRelative(-0.071f, 0.0f, -0.138f, 0.037f, -0.207f, 0.105f)
                lineTo(4.258f, 20.999f)
                lineTo(9.133f, 21.0f)
                horizontalLineTo(14.539f)
                curveTo(18.817f, 21.0f, 21.0f, 18.536f, 21.0f, 13.936f)
                verticalLineToRelative(-3.214f)
                close()
            }
        }
            .build()
        return _icDonateFlattr!!
    }

private var _icDonateFlattr: ImageVector? = null
