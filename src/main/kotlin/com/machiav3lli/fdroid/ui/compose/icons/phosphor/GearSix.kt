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

val Phosphor.GearSix: ImageVector
    get() {
        if (_gear_six != null) {
            return _gear_six!!
        }
        _gear_six = Builder(
            name = "Gear-six",
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
                moveTo(237.5f, 104.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -4.0f, -5.3f)
                lineTo(209.7f, 85.8f)
                arcToRelative(69.3f, 69.3f, 0.0f, false, false, -4.3f, -7.5f)
                lineToRelative(0.5f, -27.2f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, -2.6f, -6.1f)
                arcToRelative(112.0f, 112.0f, 0.0f, false, false, -41.1f, -23.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -6.6f, 0.8f)
                lineToRelative(-23.3f, 14.0f)
                curveToRelative(-2.9f, -0.1f, -5.7f, -0.1f, -8.6f, 0.0f)
                lineToRelative(-23.3f, -14.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -6.6f, -0.8f)
                arcTo(111.1f, 111.1f, 0.0f, false, false, 52.7f, 45.1f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -2.6f, 6.0f)
                lineToRelative(0.5f, 27.2f)
                curveToRelative(-1.6f, 2.4f, -3.0f, 4.9f, -4.4f, 7.5f)
                lineTo(22.4f, 99.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -3.9f, 5.3f)
                arcToRelative(111.4f, 111.4f, 0.0f, false, false, 0.0f, 47.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 4.0f, 5.3f)
                lineToRelative(23.8f, 13.2f)
                arcToRelative(69.3f, 69.3f, 0.0f, false, false, 4.3f, 7.5f)
                lineToRelative(-0.5f, 27.2f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, 2.6f, 6.1f)
                arcToRelative(112.0f, 112.0f, 0.0f, false, false, 41.1f, 23.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 6.6f, -0.8f)
                lineToRelative(23.3f, -14.0f)
                horizontalLineToRelative(8.6f)
                lineToRelative(23.4f, 14.0f)
                arcToRelative(7.3f, 7.3f, 0.0f, false, false, 4.1f, 1.2f)
                arcToRelative(10.0f, 10.0f, 0.0f, false, false, 2.4f, -0.4f)
                arcToRelative(111.1f, 111.1f, 0.0f, false, false, 41.1f, -23.8f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 2.6f, -6.0f)
                lineToRelative(-0.5f, -27.2f)
                curveToRelative(1.6f, -2.4f, 3.0f, -4.9f, 4.4f, -7.5f)
                lineTo(233.6f, 157.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 3.9f, -5.3f)
                arcTo(111.4f, 111.4f, 0.0f, false, false, 237.5f, 104.3f)
                close()
                moveTo(222.5f, 144.8f)
                lineTo(199.8f, 157.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, -3.3f, 3.6f)
                arcToRelative(73.6f, 73.6f, 0.0f, false, true, -5.7f, 9.8f)
                arcToRelative(8.6f, 8.6f, 0.0f, false, false, -1.4f, 4.7f)
                lineToRelative(0.4f, 25.9f)
                arcToRelative(94.4f, 94.4f, 0.0f, false, true, -29.1f, 16.9f)
                lineToRelative(-22.3f, -13.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -4.1f, -1.1f)
                horizontalLineToRelative(-0.6f)
                arcToRelative(72.3f, 72.3f, 0.0f, false, true, -11.4f, 0.0f)
                arcToRelative(8.6f, 8.6f, 0.0f, false, false, -4.7f, 1.1f)
                lineTo(95.3f, 218.3f)
                arcToRelative(95.0f, 95.0f, 0.0f, false, true, -29.1f, -16.8f)
                lineToRelative(0.4f, -26.0f)
                arcToRelative(8.6f, 8.6f, 0.0f, false, false, -1.4f, -4.7f)
                arcToRelative(86.4f, 86.4f, 0.0f, false, true, -5.7f, -9.8f)
                arcToRelative(8.8f, 8.8f, 0.0f, false, false, -3.3f, -3.6f)
                lineTo(33.5f, 144.8f)
                arcToRelative(94.8f, 94.8f, 0.0f, false, true, 0.0f, -33.6f)
                lineTo(56.2f, 98.6f)
                arcTo(8.2f, 8.2f, 0.0f, false, false, 59.5f, 95.0f)
                arcToRelative(73.6f, 73.6f, 0.0f, false, true, 5.7f, -9.8f)
                arcToRelative(8.6f, 8.6f, 0.0f, false, false, 1.4f, -4.7f)
                lineToRelative(-0.4f, -25.9f)
                arcTo(94.4f, 94.4f, 0.0f, false, true, 95.3f, 37.7f)
                lineToRelative(22.3f, 13.4f)
                arcToRelative(8.4f, 8.4f, 0.0f, false, false, 4.7f, 1.1f)
                arcToRelative(72.3f, 72.3f, 0.0f, false, true, 11.4f, 0.0f)
                arcToRelative(8.6f, 8.6f, 0.0f, false, false, 4.7f, -1.1f)
                lineToRelative(22.3f, -13.4f)
                arcToRelative(95.0f, 95.0f, 0.0f, false, true, 29.1f, 16.8f)
                lineToRelative(-0.4f, 26.0f)
                arcToRelative(8.6f, 8.6f, 0.0f, false, false, 1.4f, 4.7f)
                arcToRelative(86.4f, 86.4f, 0.0f, false, true, 5.7f, 9.8f)
                arcToRelative(8.8f, 8.8f, 0.0f, false, false, 3.3f, 3.6f)
                lineToRelative(22.7f, 12.6f)
                arcTo(94.8f, 94.8f, 0.0f, false, true, 222.5f, 144.8f)
                close()
                moveTo(128.0f, 72.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, true, false, 56.0f, 56.0f)
                arcTo(56.0f, 56.0f, 0.0f, false, false, 128.0f, 72.0f)
                close()
                moveTo(128.0f, 168.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, true, 40.0f, -40.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 128.0f, 168.0f)
                close()
            }
        }
            .build()
        return _gear_six!!
    }

private var _gear_six: ImageVector? = null
