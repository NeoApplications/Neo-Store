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

val Phosphor.Microphone: ImageVector
    get() {
        if (_microphone != null) {
            return _microphone!!
        }
        _microphone = Builder(
            name = "Microphone",
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
                moveTo(128.0f, 176.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, 48.0f, -48.0f)
                lineTo(176.0f, 64.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, -96.0f, 0.0f)
                verticalLineToRelative(64.0f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 128.0f, 176.0f)
                close()
                moveTo(96.0f, 64.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 64.0f, 0.0f)
                verticalLineToRelative(64.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -64.0f, 0.0f)
                close()
                moveTo(207.5f, 136.9f)
                arcTo(79.9f, 79.9f, 0.0f, false, true, 136.0f, 207.6f)
                lineTo(136.0f, 232.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(120.0f, 207.6f)
                arcToRelative(79.9f, 79.9f, 0.0f, false, true, -71.5f, -70.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 15.9f, -1.8f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, false, 127.2f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 15.9f, 1.8f)
                close()
            }
        }
            .build()
        return _microphone!!
    }

private var _microphone: ImageVector? = null
