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

val Phosphor.User: ImageVector
    get() {
        if (_user != null) {
            return _user!!
        }
        _user = Builder(
            name = "User",
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
                moveTo(231.9f, 212.0f)
                arcToRelative(120.7f, 120.7f, 0.0f, false, false, -67.1f, -54.2f)
                arcToRelative(72.0f, 72.0f, 0.0f, true, false, -73.6f, 0.0f)
                arcTo(120.7f, 120.7f, 0.0f, false, false, 24.1f, 212.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 13.8f, 8.0f)
                arcToRelative(104.1f, 104.1f, 0.0f, false, true, 180.2f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 13.8f, -8.0f)
                close()
                moveTo(72.0f, 96.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, true, true, 56.0f, 56.0f)
                arcTo(56.0f, 56.0f, 0.0f, false, true, 72.0f, 96.0f)
                close()
            }
        }
            .build()
        return _user!!
    }

private var _user: ImageVector? = null
