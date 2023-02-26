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

val Phosphor.Globe: ImageVector
    get() {
        if (_globe != null) {
            return _globe!!
        }
        _globe = Builder(
            name = "Globe",
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
                moveTo(225.9f, 163.2f)
                lineToRelative(0.3f, -1.0f)
                arcToRelative(103.7f, 103.7f, 0.0f, false, false, 0.0f, -68.4f)
                lineToRelative(-0.3f, -1.0f)
                arcTo(104.4f, 104.4f, 0.0f, false, false, 128.0f, 24.0f)
                horizontalLineToRelative(0.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 30.1f, 92.8f)
                lineToRelative(-0.3f, 1.0f)
                arcToRelative(103.7f, 103.7f, 0.0f, false, false, 0.0f, 68.4f)
                lineToRelative(0.3f, 1.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 232.0f)
                horizontalLineToRelative(0.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 225.9f, 163.2f)
                close()
                moveTo(102.5f, 168.0f)
                horizontalLineToRelative(51.0f)
                arcTo(108.6f, 108.6f, 0.0f, false, true, 128.0f, 210.4f)
                arcTo(108.6f, 108.6f, 0.0f, false, true, 102.5f, 168.0f)
                close()
                moveTo(98.3f, 152.0f)
                arcToRelative(126.4f, 126.4f, 0.0f, false, true, 0.0f, -48.0f)
                horizontalLineToRelative(59.4f)
                arcToRelative(127.6f, 127.6f, 0.0f, false, true, 2.3f, 24.0f)
                arcToRelative(126.8f, 126.8f, 0.0f, false, true, -2.3f, 24.0f)
                close()
                moveTo(40.0f, 128.0f)
                arcToRelative(90.3f, 90.3f, 0.0f, false, true, 3.3f, -24.0f)
                lineTo(82.0f, 104.0f)
                arcToRelative(145.0f, 145.0f, 0.0f, false, false, 0.0f, 48.0f)
                lineTo(43.3f, 152.0f)
                arcTo(90.3f, 90.3f, 0.0f, false, true, 40.0f, 128.0f)
                close()
                moveTo(153.5f, 88.0f)
                horizontalLineToRelative(-51.0f)
                arcTo(108.6f, 108.6f, 0.0f, false, true, 128.0f, 45.6f)
                arcTo(108.6f, 108.6f, 0.0f, false, true, 153.5f, 88.0f)
                close()
                moveTo(174.0f, 104.0f)
                horizontalLineToRelative(38.7f)
                arcToRelative(88.9f, 88.9f, 0.0f, false, true, 0.0f, 48.0f)
                lineTo(174.0f, 152.0f)
                arcToRelative(145.0f, 145.0f, 0.0f, false, false, 0.0f, -48.0f)
                close()
                moveTo(206.4f, 88.0f)
                horizontalLineToRelative(-36.0f)
                arcToRelative(128.7f, 128.7f, 0.0f, false, false, -24.1f, -46.1f)
                arcTo(88.6f, 88.6f, 0.0f, false, true, 206.4f, 88.0f)
                close()
                moveTo(109.7f, 41.9f)
                arcTo(128.7f, 128.7f, 0.0f, false, false, 85.6f, 88.0f)
                horizontalLineToRelative(-36.0f)
                arcTo(88.6f, 88.6f, 0.0f, false, true, 109.7f, 41.9f)
                close()
                moveTo(49.6f, 168.0f)
                horizontalLineToRelative(36.0f)
                arcToRelative(128.7f, 128.7f, 0.0f, false, false, 24.1f, 46.1f)
                arcTo(88.3f, 88.3f, 0.0f, false, true, 49.6f, 168.0f)
                close()
                moveTo(146.3f, 214.1f)
                arcTo(128.7f, 128.7f, 0.0f, false, false, 170.4f, 168.0f)
                horizontalLineToRelative(36.0f)
                arcTo(88.3f, 88.3f, 0.0f, false, true, 146.3f, 214.1f)
                close()
            }
        }
            .build()
        return _globe!!
    }

private var _globe: ImageVector? = null
