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

val Phosphor.FolderNotch: ImageVector
    get() {
        if (_folder_notch != null) {
            return _folder_notch!!
        }
        _folder_notch = Builder(
            name = "Folder-notch",
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
                moveTo(216.0f, 72.0f)
                horizontalLineTo(130.7f)
                lineTo(102.9f, 51.2f)
                arcTo(15.6f, 15.6f, 0.0f, false, false, 93.3f, 48.0f)
                horizontalLineTo(40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 24.0f, 64.0f)
                verticalLineTo(200.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineTo(216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                verticalLineTo(88.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 216.0f, 72.0f)
                close()
                moveTo(93.3f, 96.0f)
                horizontalLineTo(40.0f)
                verticalLineTo(64.0f)
                horizontalLineTo(93.3f)
                lineToRelative(21.4f, 16.0f)
                lineTo(93.3f, 96.0f)
                moveTo(216.0f, 200.0f)
                horizontalLineTo(40.0f)
                verticalLineTo(112.0f)
                horizontalLineTo(93.3f)
                arcToRelative(15.6f, 15.6f, 0.0f, false, false, 9.6f, -3.2f)
                lineTo(130.7f, 88.0f)
                horizontalLineTo(216.0f)
                close()
            }
        }
            .build()
        return _folder_notch!!
    }

private var _folder_notch: ImageVector? = null
