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

val Phosphor.Camera: ImageVector
    get() {
        if (_camera != null) {
            return _camera!!
        }
        _camera = Builder(
            name = "Camera",
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
                moveTo(208.0f, 56.0f)
                lineTo(180.3f, 56.0f)
                lineTo(166.7f, 35.6f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 160.0f, 32.0f)
                lineTo(96.0f, 32.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -6.7f, 3.6f)
                lineTo(75.7f, 56.0f)
                lineTo(48.0f, 56.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, false, 24.0f, 80.0f)
                lineTo(24.0f, 192.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, 24.0f)
                lineTo(208.0f, 216.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, -24.0f)
                lineTo(232.0f, 80.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, false, 208.0f, 56.0f)
                close()
                moveTo(216.0f, 192.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(48.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, -8.0f)
                lineTo(40.0f, 80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -8.0f)
                lineTo(80.0f, 72.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 6.7f, -3.6f)
                lineTo(100.3f, 48.0f)
                horizontalLineToRelative(55.4f)
                lineToRelative(13.6f, 20.4f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 176.0f, 72.0f)
                horizontalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, 8.0f)
                close()
                moveTo(128.0f, 88.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, false, 44.0f, 44.0f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 128.0f, 88.0f)
                close()
                moveTo(128.0f, 160.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, true, 28.0f, -28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, true, 128.0f, 160.0f)
                close()
            }
        }
            .build()
        return _camera!!
    }

private var _camera: ImageVector? = null
