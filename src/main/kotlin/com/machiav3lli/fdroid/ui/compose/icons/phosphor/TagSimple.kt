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

val Phosphor.TagSimple: ImageVector
    get() {
        if (_tag_simple != null) {
            return _tag_simple!!
        }
        _tag_simple = Builder(
            name = "Tag-simple",
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
                moveTo(187.7f, 208.0f)
                lineTo(40.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(24.0f, 64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 40.0f, 48.0f)
                lineTo(187.7f, 48.0f)
                arcTo(15.9f, 15.9f, 0.0f, false, true, 201.0f, 55.1f)
                lineToRelative(45.7f, 68.5f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, true, 0.0f, 8.8f)
                lineTo(201.0f, 200.9f)
                horizontalLineToRelative(0.0f)
                arcTo(15.9f, 15.9f, 0.0f, false, true, 187.7f, 208.0f)
                close()
                moveTo(187.7f, 192.0f)
                horizontalLineToRelative(0.0f)
                lineToRelative(42.7f, -64.0f)
                lineTo(187.7f, 64.0f)
                lineTo(40.0f, 64.0f)
                lineTo(40.0f, 192.0f)
                close()
            }
        }
            .build()
        return _tag_simple!!
    }

private var _tag_simple: ImageVector? = null
