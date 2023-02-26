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

val Phosphor.Calendar: ImageVector
    get() {
        if (_calendar != null) {
            return _calendar!!
        }
        _calendar = Builder(
            name = "Calendar",
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
                moveTo(48.0f, 48.0f)
                lineTo(72.0f, 48.0f)
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
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 96.0f)
                lineTo(208.0f, 96.0f)
                lineTo(208.0f, 208.0f)
                close()
                moveTo(128.0f, 164.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, true, -41.0f, 17.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -11.3f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 11.3f, 0.0f)
                arcTo(8.3f, 8.3f, 0.0f, false, false, 104.0f, 172.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -6.2f, -13.0f)
                lineToRelative(5.6f, -7.0f)
                lineTo(92.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(28.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 6.2f, 13.0f)
                lineToRelative(-8.8f, 11.1f)
                arcTo(23.9f, 23.9f, 0.0f, false, true, 128.0f, 164.0f)
                close()
                moveTo(168.0f, 128.0f)
                verticalLineToRelative(52.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(152.0f, 144.0f)
                lineToRelative(-3.2f, 2.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -11.2f, -1.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 1.6f, -11.2f)
                lineToRelative(16.0f, -12.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 168.0f, 128.0f)
                close()
            }
        }
            .build()
        return _calendar!!
    }

private var _calendar: ImageVector? = null
