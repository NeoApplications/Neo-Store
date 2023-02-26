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

val Phosphor.At: ImageVector
    get() {
        if (_at != null) {
            return _at!!
        }
        _at = Builder(
            name = "At",
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
                moveTo(128.0f, 24.0f)
                arcToRelative(104.0f, 104.0f, 0.0f, true, false, 57.5f, 190.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.8f, -13.4f)
                arcTo(88.0f, 88.0f, 0.0f, true, true, 216.0f, 128.0f)
                curveToRelative(0.0f, 26.4f, -10.9f, 32.0f, -20.0f, 32.0f)
                reflectiveCurveToRelative(-20.0f, -5.6f, -20.0f, -32.0f)
                lineTo(176.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(4.3f)
                arcTo(47.4f, 47.4f, 0.0f, false, false, 128.0f, 80.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, false, 37.9f, 77.4f)
                curveToRelative(6.0f, 11.9f, 16.4f, 18.6f, 30.1f, 18.6f)
                curveToRelative(22.5f, 0.0f, 36.0f, -17.9f, 36.0f, -48.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(128.0f, 160.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, true, 32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, true, 128.0f, 160.0f)
                close()
            }
        }
            .build()
        return _at!!
    }

private var _at: ImageVector? = null
