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

val Phosphor.Phone: ImageVector
    get() {
        if (_phone != null) {
            return _phone!!
        }
        _phone = Builder(
            name = "Phone",
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
                moveTo(176.0f, 224.0f)
                curveTo(96.6f, 224.0f, 32.0f, 159.4f, 32.0f, 80.0f)
                arcTo(56.2f, 56.2f, 0.0f, false, true, 80.9f, 24.4f)
                arcTo(16.3f, 16.3f, 0.0f, false, true, 97.6f, 34.0f)
                lineToRelative(20.1f, 46.9f)
                arcTo(15.9f, 15.9f, 0.0f, false, true, 116.3f, 96.0f)
                lineTo(99.7f, 121.4f)
                horizontalLineToRelative(0.0f)
                arcToRelative(76.5f, 76.5f, 0.0f, false, false, 35.2f, 35.0f)
                lineTo(160.0f, 139.7f)
                arcToRelative(15.6f, 15.6f, 0.0f, false, true, 15.1f, -1.3f)
                lineToRelative(46.9f, 20.0f)
                arcToRelative(16.3f, 16.3f, 0.0f, false, true, 9.6f, 16.7f)
                arcTo(56.2f, 56.2f, 0.0f, false, true, 176.0f, 224.0f)
                close()
                moveTo(82.9f, 40.3f)
                arcTo(40.0f, 40.0f, 0.0f, false, false, 48.0f, 80.0f)
                arcTo(128.1f, 128.1f, 0.0f, false, false, 176.0f, 208.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, 39.7f, -34.9f)
                lineToRelative(-46.9f, -20.0f)
                lineToRelative(-25.0f, 16.7f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -15.7f, 1.1f)
                arcToRelative(92.5f, 92.5f, 0.0f, false, true, -42.8f, -42.6f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 1.0f, -15.7f)
                lineTo(103.0f, 87.2f)
                close()
                moveTo(135.0f, 156.5f)
                close()
            }
        }
            .build()
        return _phone!!
    }

private var _phone: ImageVector? = null
