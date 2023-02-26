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

val Phosphor.PaintBrush: ImageVector
    get() {
        if (`_paint-brush` != null) {
            return `_paint-brush`!!
        }
        `_paint-brush` = Builder(
            name = "Paint-brush",
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
                moveTo(225.0f, 23.0f)
                curveToRelative(-21.3f, 0.0f, -45.3f, 11.8f, -71.1f, 34.9f)
                curveToRelative(-18.1f, 16.2f, -33.6f, 34.7f, -44.3f, 48.7f)
                arcTo(60.1f, 60.1f, 0.0f, false, false, 32.0f, 164.0f)
                curveToRelative(0.0f, 31.2f, -16.2f, 45.1f, -17.0f, 45.8f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, -2.5f, 8.8f)
                arcTo(7.8f, 7.8f, 0.0f, false, false, 20.0f, 224.0f)
                lineTo(92.0f, 224.0f)
                arcToRelative(60.1f, 60.1f, 0.0f, false, false, 57.4f, -77.6f)
                curveToRelative(14.0f, -10.7f, 32.5f, -26.2f, 48.7f, -44.3f)
                curveTo(221.2f, 76.3f, 233.0f, 52.3f, 233.0f, 31.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 225.0f, 23.0f)
                close()
                moveTo(92.0f, 208.0f)
                lineTo(36.6f, 208.0f)
                curveToRelative(5.7f, -9.5f, 11.4f, -24.0f, 11.4f, -44.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, true, 44.0f, 44.0f)
                close()
                moveTo(124.4f, 113.6f)
                curveToRelative(2.9f, -3.7f, 6.3f, -7.9f, 10.2f, -12.5f)
                arcToRelative(75.4f, 75.4f, 0.0f, false, true, 20.3f, 20.3f)
                curveToRelative(-4.6f, 3.9f, -8.8f, 7.3f, -12.5f, 10.2f)
                arcTo(59.4f, 59.4f, 0.0f, false, false, 124.4f, 113.6f)
                close()
                moveTo(167.0f, 110.7f)
                arcTo(93.1f, 93.1f, 0.0f, false, false, 145.3f, 89.0f)
                curveToRelative(19.6f, -21.2f, 46.0f, -44.4f, 70.8f, -49.1f)
                curveTo(211.4f, 64.7f, 188.2f, 91.1f, 167.0f, 110.7f)
                close()
            }
        }
            .build()
        return `_paint-brush`!!
    }

private var `_paint-brush`: ImageVector? = null
