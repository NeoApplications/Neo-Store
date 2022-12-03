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

val Phosphor.CrosshairSimple: ImageVector
    get() {
        if (_crosshair_simple != null) {
            return _crosshair_simple!!
        }
        _crosshair_simple = Builder(
            name = "Crosshair-simple",
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
                moveTo(128.0f, 28.0f)
                arcTo(100.0f, 100.0f, 0.0f, true, false, 228.0f, 128.0f)
                arcTo(100.2f, 100.2f, 0.0f, false, false, 128.0f, 28.0f)
                close()
                moveTo(136.0f, 211.6f)
                lineTo(136.0f, 180.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(31.6f)
                arcTo(84.2f, 84.2f, 0.0f, false, true, 44.4f, 136.0f)
                lineTo(76.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(44.4f, 120.0f)
                arcTo(84.2f, 84.2f, 0.0f, false, true, 120.0f, 44.4f)
                lineTo(120.0f, 76.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(136.0f, 44.4f)
                arcTo(84.2f, 84.2f, 0.0f, false, true, 211.6f, 120.0f)
                lineTo(180.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(31.6f)
                arcTo(84.2f, 84.2f, 0.0f, false, true, 136.0f, 211.6f)
                close()
            }
        }
            .build()
        return _crosshair_simple!!
    }

private var _crosshair_simple: ImageVector? = null
