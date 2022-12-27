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

val Phosphor.Books: ImageVector
    get() {
        if (_books != null) {
            return _books!!
        }
        _books = Builder(
            name = "Books",
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
                moveTo(233.6f, 195.6f)
                lineToRelative(-8.3f, -30.9f)
                horizontalLineToRelative(0.0f)
                lineTo(192.2f, 41.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -19.6f, -11.3f)
                lineTo(141.7f, 38.0f)
                lineToRelative(-1.0f, 0.3f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 128.0f, 32.0f)
                lineTo(96.0f, 32.0f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, false, -8.0f, 2.2f)
                arcTo(15.8f, 15.8f, 0.0f, false, false, 80.0f, 32.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(80.0f, 224.0f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, false, 8.0f, -2.2f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, false, 8.0f, 2.2f)
                horizontalLineToRelative(32.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(144.0f, 108.4f)
                lineToRelative(19.5f, 72.8f)
                horizontalLineToRelative(0.0f)
                lineToRelative(8.3f, 30.9f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 187.3f, 224.0f)
                arcToRelative(19.9f, 19.9f, 0.0f, false, false, 4.1f, -0.5f)
                lineToRelative(30.9f, -8.3f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 9.7f, -7.5f)
                arcTo(15.6f, 15.6f, 0.0f, false, false, 233.6f, 195.6f)
                close()
                moveTo(156.2f, 92.1f)
                lineToRelative(30.9f, -8.3f)
                lineToRelative(20.7f, 77.3f)
                lineToRelative(-30.9f, 8.3f)
                close()
                moveTo(176.7f, 45.2f)
                lineTo(183.0f, 68.3f)
                lineToRelative(-30.9f, 8.3f)
                lineToRelative(-6.3f, -23.1f)
                close()
                moveTo(128.0f, 48.0f)
                lineTo(128.0f, 168.0f)
                lineTo(96.0f, 168.0f)
                lineTo(96.0f, 48.0f)
                close()
                moveTo(80.0f, 48.0f)
                lineTo(80.0f, 72.0f)
                lineTo(48.0f, 72.0f)
                lineTo(48.0f, 48.0f)
                close()
                moveTo(48.0f, 208.0f)
                lineTo(48.0f, 88.0f)
                lineTo(80.0f, 88.0f)
                lineTo(80.0f, 208.0f)
                close()
                moveTo(128.0f, 208.0f)
                lineTo(96.0f, 208.0f)
                lineTo(96.0f, 184.0f)
                horizontalLineToRelative(32.0f)
                verticalLineToRelative(24.0f)
                close()
                moveTo(218.2f, 199.7f)
                lineTo(187.3f, 208.0f)
                lineTo(181.0f, 184.8f)
                lineToRelative(31.0f, -8.3f)
                lineToRelative(6.2f, 23.2f)
                close()
            }
        }
            .build()
        return _books!!
    }

private var _books: ImageVector? = null
