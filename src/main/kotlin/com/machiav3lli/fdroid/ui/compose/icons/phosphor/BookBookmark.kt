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

val Phosphor.BookBookmark: ImageVector
    get() {
        if (_book_bookmark != null) {
            return _book_bookmark!!
        }
        _book_bookmark = Builder(
            name = "Book-bookmark",
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
                moveTo(208.0f, 24.0f)
                lineTo(72.0f, 24.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 40.0f, 56.0f)
                lineTo(40.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                lineTo(192.0f, 232.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(56.0f, 216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                lineTo(208.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(216.0f, 32.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 208.0f, 24.0f)
                close()
                moveTo(120.0f, 40.0f)
                horizontalLineToRelative(48.0f)
                verticalLineToRelative(72.0f)
                lineTo(148.8f, 97.6f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -9.6f, 0.0f)
                lineTo(120.0f, 112.0f)
                close()
                moveTo(200.0f, 184.0f)
                lineTo(72.0f, 184.0f)
                arcToRelative(32.2f, 32.2f, 0.0f, false, false, -16.0f, 4.3f)
                lineTo(56.0f, 56.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 72.0f, 40.0f)
                horizontalLineToRelative(32.0f)
                verticalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 12.8f, 6.4f)
                lineTo(144.0f, 114.0f)
                lineToRelative(27.2f, 20.4f)
                arcTo(7.7f, 7.7f, 0.0f, false, false, 176.0f, 136.0f)
                arcToRelative(9.4f, 9.4f, 0.0f, false, false, 3.6f, -0.8f)
                arcTo(8.2f, 8.2f, 0.0f, false, false, 184.0f, 128.0f)
                lineTo(184.0f, 40.0f)
                horizontalLineToRelative(16.0f)
                close()
            }
        }
            .build()
        return _book_bookmark!!
    }

private var _book_bookmark: ImageVector? = null
