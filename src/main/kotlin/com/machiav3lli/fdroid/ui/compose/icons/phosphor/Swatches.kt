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

val Phosphor.Swatches: ImageVector
    get() {
        if (_swatches != null) {
            return _swatches!!
        }
        _swatches = Builder(
            name = "Swatches",
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
                moveTo(72.0f, 192.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 72.0f, 192.0f)
                close()
                moveTo(228.0f, 164.3f)
                lineTo(228.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(71.9f, 224.0f)
                arcToRelative(51.6f, 51.6f, 0.0f, false, true, -6.7f, -0.5f)
                arcToRelative(43.4f, 43.4f, 0.0f, false, true, -28.7f, -17.9f)
                arcToRelative(45.0f, 45.0f, 0.0f, false, true, -7.7f, -33.9f)
                lineTo(53.0f, 34.5f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, true, 18.5f, -13.0f)
                lineToRelative(55.2f, 9.7f)
                arcTo(16.2f, 16.2f, 0.0f, false, true, 137.0f, 37.8f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 2.6f, 12.0f)
                lineToRelative(-11.0f, 62.8f)
                lineToRelative(59.9f, -21.8f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 20.5f, 9.5f)
                lineTo(228.2f, 153.0f)
                arcTo(15.7f, 15.7f, 0.0f, false, true, 228.0f, 164.3f)
                close()
                moveTo(125.4f, 130.8f)
                lineToRelative(-10.1f, 56.8f)
                arcToRelative(41.0f, 41.0f, 0.0f, false, true, -1.8f, 7.1f)
                lineToRelative(99.6f, -36.3f)
                lineTo(194.0f, 105.8f)
                close()
                moveTo(67.7f, 207.7f)
                arcToRelative(28.3f, 28.3f, 0.0f, false, false, 31.9f, -22.8f)
                lineTo(123.9f, 47.0f)
                lineTo(68.7f, 37.3f)
                lineTo(44.5f, 174.4f)
                arcToRelative(29.0f, 29.0f, 0.0f, false, false, 5.1f, 21.9f)
                arcTo(27.5f, 27.5f, 0.0f, false, false, 67.7f, 207.7f)
                close()
                moveTo(212.0f, 208.0f)
                lineTo(212.0f, 175.9f)
                lineTo(123.7f, 208.0f)
                close()
            }
        }
            .build()
        return _swatches!!
    }

private var _swatches: ImageVector? = null
