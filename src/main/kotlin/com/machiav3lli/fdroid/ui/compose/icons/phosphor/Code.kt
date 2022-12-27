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

val Phosphor.Code: ImageVector
    get() {
        if (_code != null) {
            return _code!!
        }
        _code = Builder(
            name = "Code",
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
                moveTo(69.1f, 94.1f)
                lineTo(28.5f, 128.0f)
                lineToRelative(40.6f, 33.9f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 1.1f, 11.2f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 64.0f, 176.0f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -5.1f, -1.9f)
                lineToRelative(-48.0f, -40.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, -12.2f)
                lineToRelative(48.0f, -40.0f)
                arcTo(8.0f, 8.0f, 0.0f, true, true, 69.1f, 94.1f)
                close()
                moveTo(245.1f, 121.9f)
                lineTo(197.1f, 81.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -10.2f, 12.2f)
                lineTo(227.5f, 128.0f)
                lineToRelative(-40.6f, 33.9f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 192.0f, 176.0f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, 5.1f, -1.9f)
                lineToRelative(48.0f, -40.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 0.0f, -12.2f)
                close()
                moveTo(162.7f, 32.5f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -10.2f, 4.8f)
                lineToRelative(-64.0f, 176.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 4.8f, 10.2f)
                arcToRelative(8.6f, 8.6f, 0.0f, false, false, 2.7f, 0.5f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 7.5f, -5.3f)
                lineToRelative(64.0f, -176.0f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 162.7f, 32.5f)
                close()
            }
        }
            .build()
        return _code!!
    }

private var _code: ImageVector? = null
