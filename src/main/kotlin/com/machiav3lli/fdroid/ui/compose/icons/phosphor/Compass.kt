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

val Phosphor.Compass: ImageVector
    get() {
        if (_compass != null) {
            return _compass!!
        }
        _compass = Builder(
            name = "Compass",
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
                moveTo(128.1f, 24.0f)
                arcToRelative(104.0f, 104.0f, 0.0f, true, false, 104.0f, 104.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.1f, 24.0f)
                close()
                moveTo(128.1f, 216.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 88.0f, -88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 128.1f, 216.0f)
                close()
                moveTo(169.9f, 75.5f)
                lineToRelative(-62.1f, 28.2f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, -4.0f, 4.2f)
                lineTo(78.3f, 167.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 1.7f, 8.8f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, 5.7f, 2.3f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, false, 3.1f, -0.6f)
                lineToRelative(59.4f, -25.5f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 4.2f, -4.0f)
                lineToRelative(28.1f, -62.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -10.6f, -10.6f)
                close()
                moveTo(139.1f, 138.8f)
                lineTo(100.9f, 155.2f)
                lineTo(117.3f, 117.0f)
                lineToRelative(39.8f, -18.1f)
                close()
            }
        }
            .build()
        return _compass!!
    }

private var _compass: ImageVector? = null
