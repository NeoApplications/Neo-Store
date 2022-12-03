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

val Phosphor.ArrowCircleLeft: ImageVector
    get() {
        if (_arrow_circle_left != null) {
            return _arrow_circle_left!!
        }
        _arrow_circle_left = Builder(
            name = "Arrow-circle-left",
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
                moveTo(128.0f, 24.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, false, 232.0f, 128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(128.0f, 216.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 88.0f, -88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 128.0f, 216.0f)
                close()
                moveTo(176.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(107.3f, 136.0f)
                lineToRelative(20.3f, 20.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 11.3f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, true, -5.7f, 2.3f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -5.6f, -2.3f)
                lineToRelative(-34.0f, -33.9f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                lineToRelative(34.0f, -33.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, 11.3f)
                lineTo(107.3f, 120.0f)
                lineTo(168.0f, 120.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 176.0f, 128.0f)
                close()
            }
        }
            .build()
        return _arrow_circle_left!!
    }

private var _arrow_circle_left: ImageVector? = null
