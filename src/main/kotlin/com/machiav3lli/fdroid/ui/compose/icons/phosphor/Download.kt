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

val Phosphor.Download: ImageVector
    get() {
        if (_download != null) {
            return _download!!
        }
        _download = Builder(
            name = "Download",
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
                moveTo(240.0f, 136.0f)
                verticalLineToRelative(64.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(32.0f, 216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(16.0f, 136.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                lineTo(80.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                lineTo(32.0f, 136.0f)
                verticalLineToRelative(64.0f)
                lineTo(224.0f, 200.0f)
                lineTo(224.0f, 136.0f)
                lineTo(176.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 240.0f, 136.0f)
                close()
                moveTo(122.3f, 133.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineToRelative(48.0f, -48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, -11.4f)
                lineTo(136.0f, 108.7f)
                lineTo(136.0f, 24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(84.7f)
                lineTo(85.7f, 74.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 74.3f, 85.7f)
                close()
                moveTo(200.0f, 168.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, -12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 200.0f, 168.0f)
                close()
            }
        }
            .build()
        return _download!!
    }

private var _download: ImageVector? = null
