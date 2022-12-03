package com.machiav3lli.fdroid.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Icon

public val Icon.Opensource: ImageVector
    get() {
        if (_opensource != null) {
            return _opensource!!
        }
        _opensource = Builder(
            name = "Opensource",
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
                moveTo(24.0f, 128.061f)
                arcTo(104.0f, 104.0f, 0.0f, true, false, 127.939f, 24.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 24.0f, 128.061f)
                close()
                moveTo(216.0f, 127.948f)
                arcTo(88.0f, 88.0f, 0.0f, true, true, 127.948f, 40.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 216.0f, 127.948f)
                close()
                moveTo(127.972f, 80.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, true, 28.851f, 86.383f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -9.608f, -12.794f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, false, -38.4f, 0.023f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 1.607f, 11.199f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -11.199f, 1.607f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, true, 28.749f, -86.417f)
                close()
            }
        }
            .build()
        return _opensource!!
    }

private var _opensource: ImageVector? = null
