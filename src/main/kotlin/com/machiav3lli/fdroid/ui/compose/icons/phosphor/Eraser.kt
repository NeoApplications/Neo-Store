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

val Phosphor.Eraser: ImageVector
    get() {
        if (_eraser != null) {
            return _eraser!!
        }
        _eraser = Builder(
            name = "Eraser",
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
                moveTo(216.0f, 207.8f)
                horizontalLineTo(130.3f)
                lineToRelative(34.8f, -34.7f)
                horizontalLineToRelative(0.0f)
                lineToRelative(56.6f, -56.6f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 0.0f, -33.9f)
                lineTo(176.4f, 37.3f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, -33.9f, 0.0f)
                lineTo(85.9f, 93.9f)
                horizontalLineToRelative(0.0f)
                lineTo(29.3f, 150.5f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, 0.0f, 33.9f)
                lineToRelative(37.1f, 37.1f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 5.7f, 2.3f)
                horizontalLineTo(216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(153.8f, 48.6f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.3f, 0.0f)
                lineToRelative(45.2f, 45.3f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, 11.3f)
                lineToRelative(-50.9f, 50.9f)
                lineTo(102.9f, 99.5f)
                close()
                moveTo(75.4f, 207.8f)
                lineTo(40.6f, 173.1f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.3f)
                lineToRelative(51.0f, -50.9f)
                lineToRelative(56.5f, 56.5f)
                lineToRelative(-40.4f, 40.4f)
                close()
            }
        }
            .build()
        return _eraser!!
    }

private var _eraser: ImageVector? = null
