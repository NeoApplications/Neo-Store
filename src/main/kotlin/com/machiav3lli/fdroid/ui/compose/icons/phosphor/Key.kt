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

val Phosphor.Key: ImageVector
    get() {
        if (_key != null) {
            return _key!!
        }
        _key = Builder(
            name = "Key",
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
                moveTo(160.0f, 16.0f)
                arcTo(80.1f, 80.1f, 0.0f, false, false, 83.9f, 120.8f)
                lineTo(26.3f, 178.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 24.0f, 184.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                lineTo(72.0f, 232.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(80.0f, 208.0f)
                lineTo(96.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(104.0f, 184.0f)
                horizontalLineToRelative(16.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 5.7f, -2.3f)
                lineToRelative(9.5f, -9.6f)
                arcTo(80.0f, 80.0f, 0.0f, true, false, 160.0f, 16.0f)
                close()
                moveTo(160.0f, 160.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, -23.7f, -4.5f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -8.8f, 1.7f)
                lineTo(116.7f, 168.0f)
                lineTo(96.0f, 168.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(16.0f)
                lineTo(72.0f, 192.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(16.0f)
                lineTo(40.0f, 216.0f)
                lineTo(40.0f, 187.3f)
                lineToRelative(58.8f, -58.8f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 1.7f, -8.8f)
                arcTo(64.0f, 64.0f, 0.0f, true, true, 160.0f, 160.0f)
                close()
                moveTo(192.0f, 76.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 192.0f, 76.0f)
                close()
            }
        }
            .build()
        return _key!!
    }

private var _key: ImageVector? = null
