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

val Phosphor.DeviceMobile: ImageVector
    get() {
        if (_device_mobile != null) {
            return _device_mobile!!
        }
        _device_mobile = Builder(
            name = "Device-mobile",
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
                moveTo(176.0f, 16.0f)
                lineTo(80.0f, 16.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, false, 56.0f, 40.0f)
                lineTo(56.0f, 216.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, 24.0f)
                horizontalLineToRelative(96.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, -24.0f)
                lineTo(200.0f, 40.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, false, 176.0f, 16.0f)
                close()
                moveTo(72.0f, 64.0f)
                lineTo(184.0f, 64.0f)
                lineTo(184.0f, 192.0f)
                lineTo(72.0f, 192.0f)
                close()
                moveTo(80.0f, 32.0f)
                horizontalLineToRelative(96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, 8.0f)
                verticalLineToRelative(8.0f)
                lineTo(72.0f, 48.0f)
                lineTo(72.0f, 40.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 80.0f, 32.0f)
                close()
                moveTo(176.0f, 224.0f)
                lineTo(80.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, -8.0f)
                verticalLineToRelative(-8.0f)
                lineTo(184.0f, 208.0f)
                verticalLineToRelative(8.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 176.0f, 224.0f)
                close()
            }
        }
            .build()
        return _device_mobile!!
    }

private var _device_mobile: ImageVector? = null
