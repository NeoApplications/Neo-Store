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

val Phosphor.ChartLine: ImageVector
    get() {
        if (_chart_line != null) {
            return _chart_line!!
        }
        _chart_line = Builder(
            name = "Chart-line",
            defaultWidth = 32.0.dp,
            defaultHeight = 32.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(232.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, -8.0f)
                verticalLineTo(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(94.4f)
                lineTo(90.7f, 98.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 10.1f, -0.4f)
                lineToRelative(58.8f, 44.1f)
                lineTo(218.7f, 90.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 10.6f, 12.0f)
                lineToRelative(-64.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -10.1f, 0.4f)
                lineTo(96.4f, 114.3f)
                lineTo(40.0f, 163.6f)
                verticalLineTo(200.0f)
                horizontalLineTo(224.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 232.0f, 208.0f)
                close()
            }
        }
            .build()
        return _chart_line!!
    }

private var _chart_line: ImageVector? = null
