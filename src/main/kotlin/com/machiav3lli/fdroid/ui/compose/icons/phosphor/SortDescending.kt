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

val Phosphor.SortDescending: ImageVector
    get() {
        if (_SortDescending != null) {
            return _SortDescending!!
        }
        _SortDescending = Builder(
            name = "Sort-descending",
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
                moveTo(229.7f, 93.6f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -11.4f, 0.1f)
                lineTo(192.0f, 67.3f)
                lineTo(192.0f, 144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(176.0f, 67.3f)
                lineTo(149.7f, 93.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                lineToRelative(40.0f, -40.0f)
                horizontalLineToRelative(0.1f)
                curveToRelative(0.2f, -0.2f, 0.4f, -0.3f, 0.5f, -0.5f)
                lineToRelative(0.3f, -0.2f)
                lineToRelative(0.4f, -0.3f)
                lineToRelative(0.3f, -0.2f)
                lineToRelative(0.3f, -0.2f)
                horizontalLineToRelative(0.4f)
                lineToRelative(0.3f, -0.2f)
                horizontalLineToRelative(0.4f)
                lineToRelative(0.4f, -0.2f)
                horizontalLineToRelative(4.6f)
                lineToRelative(0.4f, 0.2f)
                horizontalLineToRelative(0.4f)
                lineToRelative(0.3f, 0.2f)
                horizontalLineToRelative(0.4f)
                lineToRelative(0.3f, 0.2f)
                lineToRelative(0.3f, 0.2f)
                lineToRelative(0.4f, 0.3f)
                lineToRelative(0.3f, 0.2f)
                curveToRelative(0.2f, 0.2f, 0.4f, 0.3f, 0.5f, 0.5f)
                horizontalLineToRelative(0.1f)
                lineToRelative(40.0f, 40.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 229.7f, 93.6f)
                close()
                moveTo(48.0f, 136.0f)
                horizontalLineToRelative(72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(48.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                close()
                moveTo(48.0f, 72.0f)
                horizontalLineToRelative(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(48.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                close()
                moveTo(184.0f, 184.0f)
                lineTo(48.0f, 184.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(184.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
            }
        }
            .build()
        return _SortDescending!!
    }

private var _SortDescending: ImageVector? = null
