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

val Phosphor.UserGear: ImageVector
    get() {
        if (_user_gear != null) {
            return _user_gear!!
        }
        _user_gear = Builder(
            name = "User-gear",
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
                moveTo(144.1f, 157.6f)
                arcToRelative(68.0f, 68.0f, 0.0f, true, false, -72.2f, 0.0f)
                arcToRelative(118.4f, 118.4f, 0.0f, false, false, -55.8f, 37.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 12.2f, 10.2f)
                arcToRelative(104.2f, 104.2f, 0.0f, false, true, 159.4f, 0.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 11.2f, 1.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 1.0f, -11.2f)
                arcTo(118.4f, 118.4f, 0.0f, false, false, 144.1f, 157.6f)
                close()
                moveTo(56.0f, 100.0f)
                arcToRelative(52.0f, 52.0f, 0.0f, true, true, 52.0f, 52.0f)
                arcTo(52.0f, 52.0f, 0.0f, false, true, 56.0f, 100.0f)
                close()
                moveTo(248.2f, 143.1f)
                lineTo(243.6f, 140.4f)
                arcToRelative(24.4f, 24.4f, 0.0f, false, false, 0.0f, -8.8f)
                lineToRelative(4.6f, -2.7f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 3.0f, -10.9f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.0f, -2.9f)
                lineToRelative(-4.6f, 2.7f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, -7.6f, -4.4f)
                lineTo(228.0f, 108.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(5.4f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, -7.6f, 4.4f)
                lineToRelative(-4.6f, -2.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.0f, 2.9f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 3.0f, 10.9f)
                lineToRelative(4.6f, 2.7f)
                arcToRelative(24.4f, 24.4f, 0.0f, false, false, 0.0f, 8.8f)
                lineToRelative(-4.6f, 2.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 4.0f, 14.9f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, false, 4.0f, -1.1f)
                lineToRelative(4.6f, -2.7f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, 7.6f, 4.4f)
                lineTo(212.0f, 164.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineToRelative(-5.4f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, 7.6f, -4.4f)
                lineToRelative(4.6f, 2.7f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, false, 4.0f, 1.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 4.0f, -14.9f)
                close()
                moveTo(212.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 8.0f, 8.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 212.0f, 136.0f)
                close()
            }
        }
            .build()
        return _user_gear!!
    }

private var _user_gear: ImageVector? = null
