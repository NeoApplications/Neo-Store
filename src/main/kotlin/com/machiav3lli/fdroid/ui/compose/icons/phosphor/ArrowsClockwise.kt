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

val Phosphor.ArrowsClockwise: ImageVector
    get() {
        if (_arrows_clockwise != null) {
            return _arrows_clockwise!!
        }
        _arrows_clockwise = Builder(
            name = "Arrows-clockwise",
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
                moveTo(232.2f, 51.7f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineToRelative(-48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(28.7f)
                lineTo(184.6f, 71.4f)
                arcToRelative(80.2f, 80.2f, 0.0f, false, false, -113.2f, 0.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, -11.3f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -11.3f)
                arcToRelative(96.2f, 96.2f, 0.0f, false, true, 135.8f, 0.0f)
                lineToRelative(20.3f, 20.3f)
                verticalLineTo(51.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 16.0f, 0.0f)
                close()
                moveTo(184.6f, 184.6f)
                arcToRelative(80.2f, 80.2f, 0.0f, false, true, -113.2f, 0.0f)
                lineTo(51.1f, 164.3f)
                horizontalLineTo(79.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 0.0f, -16.0f)
                horizontalLineToRelative(-48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(175.6f)
                lineToRelative(20.3f, 20.3f)
                arcToRelative(96.1f, 96.1f, 0.0f, false, false, 135.8f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -11.3f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 184.6f, 184.6f)
                close()
            }
        }
            .build()
        return _arrows_clockwise!!
    }

private var _arrows_clockwise: ImageVector? = null
