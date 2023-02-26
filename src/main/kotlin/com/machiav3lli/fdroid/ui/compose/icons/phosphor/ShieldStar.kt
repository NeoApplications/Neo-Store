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

val Phosphor.ShieldStar: ImageVector
    get() {
        if (_shield_star != null) {
            return _shield_star!!
        }
        _shield_star = Builder(
            name = "Shield-star",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(208.0f, 40.0f)
                lineTo(48.0f, 40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 56.0f)
                verticalLineToRelative(58.7f)
                curveToRelative(0.0f, 89.4f, 75.8f, 119.1f, 91.0f, 124.1f)
                arcToRelative(14.3f, 14.3f, 0.0f, false, false, 10.0f, 0.0f)
                curveToRelative(15.2f, -5.0f, 91.0f, -34.7f, 91.0f, -124.1f)
                lineTo(224.0f, 56.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 40.0f)
                close()
                moveTo(208.0f, 114.7f)
                curveToRelative(0.0f, 78.2f, -66.4f, 104.4f, -80.0f, 108.9f)
                curveToRelative(-13.5f, -4.5f, -80.0f, -30.6f, -80.0f, -108.9f)
                lineTo(48.0f, 56.0f)
                lineTo(208.0f, 56.0f)
                close()
                moveTo(82.3f, 121.2f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 92.4f, 116.0f)
                lineToRelative(27.6f, 9.0f)
                lineTo(120.0f, 96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(29.0f)
                lineToRelative(27.6f, -9.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 10.1f, 5.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.2f, 10.0f)
                lineToRelative(-27.6f, 9.0f)
                lineTo(158.0f, 163.7f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, -1.8f, 11.1f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -4.7f, 1.6f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -6.5f, -3.3f)
                lineToRelative(-17.0f, -23.5f)
                lineToRelative(-17.0f, 23.5f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -6.5f, 3.3f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -4.7f, -1.6f)
                arcTo(7.9f, 7.9f, 0.0f, false, true, 98.0f, 163.7f)
                lineToRelative(17.1f, -23.5f)
                lineToRelative(-27.6f, -9.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 82.3f, 121.2f)
                close()
            }
        }
            .build()
        return _shield_star!!
    }

private var _shield_star: ImageVector? = null
