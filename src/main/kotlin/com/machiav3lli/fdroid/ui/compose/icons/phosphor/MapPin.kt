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

val Phosphor.MapPin: ImageVector
    get() {
        if (_map_pin != null) {
            return _map_pin!!
        }
        _map_pin = Builder(
            name = "Map-pin",
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
                moveTo(128.1f, 64.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, false, 40.0f, 40.0f)
                arcTo(40.1f, 40.1f, 0.0f, false, false, 128.1f, 64.0f)
                close()
                moveTo(128.1f, 128.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, true, 24.0f, -24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 128.1f, 128.0f)
                close()
                moveTo(128.1f, 16.0f)
                arcToRelative(88.1f, 88.1f, 0.0f, false, false, -88.0f, 88.0f)
                curveToRelative(0.0f, 31.4f, 14.5f, 64.7f, 42.0f, 96.2f)
                arcToRelative(259.4f, 259.4f, 0.0f, false, false, 41.4f, 38.4f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, 9.2f, 0.0f)
                arcToRelative(257.6f, 257.6f, 0.0f, false, false, 41.5f, -38.4f)
                curveToRelative(27.4f, -31.5f, 41.9f, -64.8f, 41.9f, -96.2f)
                arcTo(88.1f, 88.1f, 0.0f, false, false, 128.1f, 16.0f)
                close()
                moveTo(128.1f, 222.0f)
                curveToRelative(-16.5f, -13.0f, -72.0f, -60.8f, -72.0f, -118.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, false, true, 144.0f, 0.0f)
                curveTo(200.1f, 161.2f, 144.6f, 209.0f, 128.1f, 222.0f)
                close()
            }
        }
            .build()
        return _map_pin!!
    }

private var _map_pin: ImageVector? = null
