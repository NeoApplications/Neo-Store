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

val Phosphor.Chat: ImageVector
    get() {
        if (_chat != null) {
            return _chat!!
        }
        _chat = Builder(
            name = "Chat",
            defaultWidth = 32.0.dp,
            defaultHeight = 32.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(40.0f, 238.8f)
                arcToRelative(16.4f, 16.4f, 0.0f, false, true, -6.8f, -1.5f)
                arcTo(15.7f, 15.7f, 0.0f, false, true, 24.0f, 222.8f)
                verticalLineTo(64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 40.0f, 48.0f)
                horizontalLineTo(216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                verticalLineTo(192.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                horizontalLineTo(82.5f)
                lineTo(50.3f, 235.1f)
                arcTo(15.9f, 15.9f, 0.0f, false, true, 40.0f, 238.8f)
                close()
                moveTo(40.0f, 64.0f)
                verticalLineTo(222.8f)
                lineToRelative(32.2f, -27.0f)
                arcTo(15.6f, 15.6f, 0.0f, false, true, 82.5f, 192.0f)
                horizontalLineTo(216.0f)
                verticalLineTo(64.0f)
                close()
                moveTo(77.4f, 201.9f)
                close()
            }
        }
            .build()
        return _chat!!
    }

private var _chat: ImageVector? = null
