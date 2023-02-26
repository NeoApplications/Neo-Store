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

val Phosphor.Check: ImageVector
    get() {
        if (_check != null) {
            return _check!!
        }
        _check = Builder(
            name = "Check",
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
                moveTo(104.0f, 192.0f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, true, -5.7f, -2.3f)
                lineToRelative(-56.0f, -56.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(104.0f, 172.7f)
                lineTo(210.3f, 66.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 11.4f)
                lineToRelative(-112.0f, 112.0f)
                arcTo(8.5f, 8.5f, 0.0f, false, true, 104.0f, 192.0f)
                close()
            }
        }
            .build()
        return _check!!
    }

private var _check: ImageVector? = null
