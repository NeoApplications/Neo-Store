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

val Phosphor.UserFocus: ImageVector
    get() {
        if (_user_focus != null) {
            return _user_focus!!
        }
        _user_focus = Builder(
            name = "User-focus",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(224.0f, 48.0f)
                lineTo(224.0f, 76.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(208.0f, 48.0f)
                lineTo(180.0f, 48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(28.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 224.0f, 48.0f)
                close()
                moveTo(216.0f, 172.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(28.0f)
                lineTo(180.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(28.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 180.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 216.0f, 172.0f)
                close()
                moveTo(76.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 180.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(28.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(76.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(40.0f, 84.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(48.0f, 48.0f)
                lineTo(76.0f, 48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 76.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 40.0f, 84.0f)
                close()
                moveTo(174.0f, 179.7f)
                arcToRelative(52.0f, 52.0f, 0.0f, false, false, -92.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -14.2f, -7.4f)
                arcToRelative(69.1f, 69.1f, 0.0f, false, true, 33.3f, -30.8f)
                arcTo(39.5f, 39.5f, 0.0f, false, true, 88.0f, 112.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, 80.0f, 0.0f)
                arcToRelative(39.5f, 39.5f, 0.0f, false, true, -13.1f, 29.5f)
                arcToRelative(69.1f, 69.1f, 0.0f, false, true, 33.3f, 30.8f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -3.4f, 10.8f)
                arcToRelative(8.4f, 8.4f, 0.0f, false, true, -3.7f, 0.9f)
                arcTo(7.8f, 7.8f, 0.0f, false, true, 174.0f, 179.7f)
                close()
                moveTo(128.0f, 136.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, false, -24.0f, -24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, false, 128.0f, 136.0f)
                close()
            }
        }
            .build()
        return _user_focus!!
    }

private var _user_focus: ImageVector? = null
