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

val Phosphor.EyeSlash: ImageVector
    get() {
        if (_eye_slash != null) {
            return _eye_slash!!
        }
        _eye_slash = Builder(
            name = "Eye-slash",
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
                moveTo(53.9f, 34.6f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 42.1f, 45.4f)
                lineTo(61.3f, 66.5f)
                curveTo(25.0f, 88.8f, 9.4f, 123.2f, 8.7f, 124.8f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 0.0f, 6.5f)
                curveToRelative(0.3f, 0.7f, 8.8f, 19.5f, 27.6f, 38.4f)
                curveTo(61.4f, 194.7f, 93.1f, 208.0f, 128.0f, 208.0f)
                arcToRelative(128.6f, 128.6f, 0.0f, false, false, 52.1f, -10.8f)
                lineToRelative(22.0f, 24.2f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 208.0f, 224.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 5.4f, -2.1f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 0.5f, -11.3f)
                close()
                moveTo(101.2f, 110.5f)
                lineTo(142.9f, 156.3f)
                arcTo(31.6f, 31.6f, 0.0f, false, true, 128.0f, 160.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -26.8f, -49.5f)
                close()
                moveTo(128.0f, 192.0f)
                curveToRelative(-30.8f, 0.0f, -57.7f, -11.2f, -79.9f, -33.3f)
                arcTo(128.3f, 128.3f, 0.0f, false, true, 25.0f, 128.0f)
                curveToRelative(4.7f, -8.8f, 19.8f, -33.5f, 47.3f, -49.4f)
                lineToRelative(18.0f, 19.8f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, 63.6f, 70.0f)
                lineToRelative(14.7f, 16.2f)
                arcTo(112.1f, 112.1f, 0.0f, false, true, 128.0f, 192.0f)
                close()
                moveTo(247.3f, 131.3f)
                curveToRelative(-0.4f, 0.9f, -10.5f, 23.3f, -33.4f, 43.8f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -5.3f, 2.0f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -5.9f, -2.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.6f, -11.3f)
                arcTo(131.0f, 131.0f, 0.0f, false, false, 231.0f, 128.0f)
                arcToRelative(130.3f, 130.3f, 0.0f, false, false, -23.1f, -30.8f)
                curveTo(185.7f, 75.2f, 158.8f, 64.0f, 128.0f, 64.0f)
                arcToRelative(112.9f, 112.9f, 0.0f, false, false, -19.4f, 1.6f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 99.4f, 59.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 6.6f, -9.2f)
                arcTo(132.4f, 132.4f, 0.0f, false, true, 128.0f, 48.0f)
                curveToRelative(34.9f, 0.0f, 66.6f, 13.3f, 91.7f, 38.3f)
                curveToRelative(18.8f, 18.9f, 27.3f, 37.7f, 27.6f, 38.5f)
                arcTo(8.2f, 8.2f, 0.0f, false, true, 247.3f, 131.3f)
                close()
                moveTo(134.0f, 96.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 3.0f, -15.8f)
                arcToRelative(48.3f, 48.3f, 0.0f, false, true, 38.8f, 42.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -7.2f, 8.7f)
                horizontalLineToRelative(-0.8f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, -7.9f, -7.2f)
                arcTo(32.2f, 32.2f, 0.0f, false, false, 134.0f, 96.6f)
                close()
            }
        }
            .build()
        return _eye_slash!!
    }

private var _eye_slash: ImageVector? = null
