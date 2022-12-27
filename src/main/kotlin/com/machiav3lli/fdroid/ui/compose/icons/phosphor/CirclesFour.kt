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

val Phosphor.CirclesFour: ImageVector
    get() {
        if (_circles_four != null) {
            return _circles_four!!
        }
        _circles_four = Builder(
            name = "Circles-four",
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
                moveTo(76.0f, 32.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, false, 44.0f, 44.0f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 76.0f, 32.0f)
                close()
                moveTo(76.0f, 104.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, true, 28.0f, -28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, true, 76.0f, 104.0f)
                close()
                moveTo(180.0f, 120.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, false, -44.0f, -44.0f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 180.0f, 120.0f)
                close()
                moveTo(180.0f, 48.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, true, -28.0f, 28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, true, 180.0f, 48.0f)
                close()
                moveTo(76.0f, 136.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, false, 44.0f, 44.0f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 76.0f, 136.0f)
                close()
                moveTo(76.0f, 208.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, true, 28.0f, -28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, true, 76.0f, 208.0f)
                close()
                moveTo(180.0f, 136.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, false, 44.0f, 44.0f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 180.0f, 136.0f)
                close()
                moveTo(180.0f, 208.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, true, 28.0f, -28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, true, 180.0f, 208.0f)
                close()
            }
        }
            .build()
        return _circles_four!!
    }

private var _circles_four: ImageVector? = null
