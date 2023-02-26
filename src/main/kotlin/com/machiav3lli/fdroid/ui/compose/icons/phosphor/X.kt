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

val Phosphor.X: ImageVector
    get() {
        if (_x != null) {
            return _x!!
        }
        _x = Builder(
            name = "X",
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
                moveTo(205.7f, 194.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineTo(128.0f, 139.3f)
                lineTo(61.7f, 205.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                lineTo(116.7f, 128.0f)
                lineTo(50.3f, 61.7f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 61.7f, 50.3f)
                lineTo(128.0f, 116.7f)
                lineToRelative(66.3f, -66.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 11.4f)
                lineTo(139.3f, 128.0f)
                close()
            }
        }
            .build()
        return _x!!
    }

private var _x: ImageVector? = null
