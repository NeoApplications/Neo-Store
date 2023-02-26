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

val Phosphor.CaretUp: ImageVector
    get() {
        if (_caret_up != null) {
            return _caret_up!!
        }
        _caret_up = Builder(
            name = "Caret-up",
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
                moveTo(208.0f, 168.0f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, true, -5.7f, -2.3f)
                lineTo(128.0f, 91.3f)
                lineTo(53.7f, 165.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -11.4f, -11.4f)
                lineToRelative(80.0f, -80.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 0.0f)
                lineToRelative(80.0f, 80.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcTo(8.5f, 8.5f, 0.0f, false, true, 208.0f, 168.0f)
                close()
            }
        }
            .build()
        return _caret_up!!
    }

private var _caret_up: ImageVector? = null
