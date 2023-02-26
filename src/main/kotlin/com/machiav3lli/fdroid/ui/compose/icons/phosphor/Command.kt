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

val Phosphor.Command: ImageVector
    get() {
        if (_command != null) {
            return _command!!
        }
        _command = Builder(
            name = "Command",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(180.0f, 144.0f)
                lineTo(160.0f, 144.0f)
                lineTo(160.0f, 112.0f)
                horizontalLineToRelative(20.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, -36.0f, -36.0f)
                lineTo(144.0f, 96.0f)
                lineTo(112.0f, 96.0f)
                lineTo(112.0f, 76.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, -36.0f, 36.0f)
                lineTo(96.0f, 112.0f)
                verticalLineToRelative(32.0f)
                lineTo(76.0f, 144.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 36.0f, 36.0f)
                lineTo(112.0f, 160.0f)
                horizontalLineToRelative(32.0f)
                verticalLineToRelative(20.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 36.0f, -36.0f)
                close()
                moveTo(160.0f, 76.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, 20.0f, 20.0f)
                lineTo(160.0f, 96.0f)
                close()
                moveTo(56.0f, 76.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, false, true, 40.0f, 0.0f)
                lineTo(96.0f, 96.0f)
                lineTo(76.0f, 96.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 56.0f, 76.0f)
                close()
                moveTo(96.0f, 180.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, -20.0f, -20.0f)
                lineTo(96.0f, 160.0f)
                close()
                moveTo(112.0f, 112.0f)
                horizontalLineToRelative(32.0f)
                verticalLineToRelative(32.0f)
                lineTo(112.0f, 144.0f)
                close()
                moveTo(180.0f, 200.0f)
                arcToRelative(20.1f, 20.1f, 0.0f, false, true, -20.0f, -20.0f)
                lineTo(160.0f, 160.0f)
                horizontalLineToRelative(20.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, false, true, 0.0f, 40.0f)
                close()
            }
        }
            .build()
        return _command!!
    }

private var _command: ImageVector? = null
