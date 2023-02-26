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

val Phosphor.Broadcast: ImageVector
    get() {
        if (_broadcast != null) {
            return _broadcast!!
        }
        _broadcast = Builder(
            name = "Broadcast",
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
                moveTo(128.0f, 88.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, false, 40.0f, 40.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, false, 128.0f, 88.0f)
                close()
                moveTo(128.0f, 152.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, true, 24.0f, -24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 128.0f, 152.0f)
                close()
                moveTo(69.0f, 103.1f)
                arcToRelative(64.5f, 64.5f, 0.0f, false, false, 0.0f, 49.8f)
                arcToRelative(65.4f, 65.4f, 0.0f, false, false, 13.7f, 20.4f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, 11.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.6f, 2.3f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, true, -5.7f, -2.3f)
                arcToRelative(80.0f, 80.0f, 0.0f, false, true, -17.1f, -25.5f)
                arcToRelative(79.9f, 79.9f, 0.0f, false, true, 0.0f, -62.2f)
                arcTo(80.0f, 80.0f, 0.0f, false, true, 71.4f, 71.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, 0.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, 11.3f)
                arcTo(65.4f, 65.4f, 0.0f, false, false, 69.0f, 103.1f)
                close()
                moveTo(201.7f, 159.1f)
                arcToRelative(80.0f, 80.0f, 0.0f, false, true, -17.1f, 25.5f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, true, -5.7f, 2.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.6f, -2.3f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, -11.3f)
                arcTo(65.4f, 65.4f, 0.0f, false, false, 187.0f, 152.9f)
                arcToRelative(64.5f, 64.5f, 0.0f, false, false, 0.0f, -49.8f)
                arcToRelative(65.4f, 65.4f, 0.0f, false, false, -13.7f, -20.4f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, -11.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, 0.0f)
                arcToRelative(80.0f, 80.0f, 0.0f, false, true, 17.1f, 25.5f)
                arcToRelative(79.9f, 79.9f, 0.0f, false, true, 0.0f, 62.2f)
                close()
                moveTo(54.5f, 201.5f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, true, -5.7f, 2.3f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, true, -5.7f, -2.3f)
                arcToRelative(121.8f, 121.8f, 0.0f, false, true, -25.7f, -38.2f)
                arcToRelative(120.7f, 120.7f, 0.0f, false, true, 0.0f, -93.4f)
                arcTo(121.8f, 121.8f, 0.0f, false, true, 43.1f, 43.1f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 54.5f, 54.5f)
                arcTo(103.5f, 103.5f, 0.0f, false, false, 24.0f, 128.0f)
                arcToRelative(103.5f, 103.5f, 0.0f, false, false, 30.5f, 73.5f)
                close()
                moveTo(248.0f, 128.0f)
                arcToRelative(120.2f, 120.2f, 0.0f, false, true, -9.4f, 46.7f)
                arcToRelative(121.8f, 121.8f, 0.0f, false, true, -25.7f, 38.2f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, true, -5.7f, 2.3f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, true, -5.7f, -2.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                arcTo(103.5f, 103.5f, 0.0f, false, false, 232.0f, 128.0f)
                arcToRelative(103.5f, 103.5f, 0.0f, false, false, -30.5f, -73.5f)
                arcToRelative(8.1f, 8.1f, 0.0f, true, true, 11.4f, -11.4f)
                arcToRelative(121.8f, 121.8f, 0.0f, false, true, 25.7f, 38.2f)
                arcTo(120.2f, 120.2f, 0.0f, false, true, 248.0f, 128.0f)
                close()
            }
        }
            .build()
        return _broadcast!!
    }

private var _broadcast: ImageVector? = null
