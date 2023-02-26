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

val Phosphor.Graph: ImageVector
    get() {
        if (_graph != null) {
            return _graph!!
        }
        _graph = Builder(
            name = "Graph",
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
                moveTo(200.0f, 152.0f)
                arcToRelative(31.7f, 31.7f, 0.0f, false, false, -19.5f, 6.7f)
                lineToRelative(-23.1f, -18.0f)
                arcTo(31.7f, 31.7f, 0.0f, false, false, 160.0f, 128.0f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, -0.1f, -2.2f)
                lineToRelative(13.3f, -4.4f)
                arcTo(31.9f, 31.9f, 0.0f, true, false, 168.0f, 104.0f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, 0.1f, 2.2f)
                lineToRelative(-13.3f, 4.4f)
                arcTo(31.9f, 31.9f, 0.0f, false, false, 128.0f, 96.0f)
                arcToRelative(45.5f, 45.5f, 0.0f, false, false, -5.3f, 0.4f)
                lineTo(115.9f, 81.0f)
                arcTo(31.7f, 31.7f, 0.0f, false, false, 128.0f, 56.0f)
                arcTo(32.0f, 32.0f, 0.0f, true, false, 96.0f, 88.0f)
                arcToRelative(45.5f, 45.5f, 0.0f, false, false, 5.3f, -0.4f)
                lineToRelative(6.8f, 15.4f)
                arcTo(31.7f, 31.7f, 0.0f, false, false, 96.0f, 128.0f)
                arcToRelative(32.4f, 32.4f, 0.0f, false, false, 3.5f, 14.6f)
                lineTo(73.8f, 165.4f)
                arcTo(32.0f, 32.0f, 0.0f, true, false, 88.0f, 192.0f)
                arcToRelative(32.4f, 32.4f, 0.0f, false, false, -3.5f, -14.6f)
                lineToRelative(25.7f, -22.8f)
                arcToRelative(31.9f, 31.9f, 0.0f, false, false, 37.3f, -1.3f)
                lineToRelative(23.1f, 18.0f)
                arcTo(31.7f, 31.7f, 0.0f, false, false, 168.0f, 184.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, false, 32.0f, -32.0f)
                close()
                moveTo(200.0f, 88.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, -16.0f, 16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 200.0f, 88.0f)
                close()
                moveTo(80.0f, 56.0f)
                arcTo(16.0f, 16.0f, 0.0f, true, true, 96.0f, 72.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 80.0f, 56.0f)
                close()
                moveTo(56.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 16.0f, -16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 56.0f, 208.0f)
                close()
                moveTo(112.0f, 128.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 16.0f, 16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 112.0f, 128.0f)
                close()
                moveTo(200.0f, 200.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 16.0f, -16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 200.0f, 200.0f)
                close()
            }
        }
            .build()
        return _graph!!
    }

private var _graph: ImageVector? = null
