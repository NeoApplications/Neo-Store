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

val Phosphor.CalendarX: ImageVector
    get() {
        if (_calendar_x != null) {
            return _calendar_x!!
        }
        _calendar_x = Builder(
            name = "Calendar-x",
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
                moveTo(208.0f, 32.0f)
                lineTo(184.0f, 32.0f)
                lineTo(184.0f, 24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                lineTo(88.0f, 32.0f)
                lineTo(88.0f, 24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 32.0f)
                close()
                moveTo(72.0f, 48.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(88.0f, 48.0f)
                horizontalLineToRelative(80.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(184.0f, 48.0f)
                horizontalLineToRelative(24.0f)
                lineTo(208.0f, 80.0f)
                lineTo(48.0f, 80.0f)
                lineTo(48.0f, 48.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 96.0f)
                lineTo(208.0f, 96.0f)
                lineTo(208.0f, 208.0f)
                close()
                moveTo(157.7f, 133.7f)
                lineTo(139.3f, 152.0f)
                lineToRelative(18.4f, 18.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineTo(128.0f, 163.3f)
                lineToRelative(-18.3f, 18.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                lineTo(116.7f, 152.0f)
                lineTo(98.3f, 133.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(128.0f, 140.7f)
                lineToRelative(18.3f, -18.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 11.4f)
                close()
            }
        }
            .build()
        return _calendar_x!!
    }

private var _calendar_x: ImageVector? = null
