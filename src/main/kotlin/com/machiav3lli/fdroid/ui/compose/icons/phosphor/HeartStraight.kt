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

val Phosphor.HeartStraight: ImageVector
    get() {
        if (_heart_straight != null) {
            return _heart_straight!!
        }
        _heart_straight = Builder(
            name = "Heart-straight",
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
                moveTo(128.0f, 222.2f)
                arcToRelative(15.6f, 15.6f, 0.0f, false, true, -11.3f, -4.7f)
                lineTo(33.6f, 134.4f)
                arcToRelative(59.9f, 59.9f, 0.0f, false, true, 2.3f, -87.0f)
                arcTo(57.7f, 57.7f, 0.0f, false, true, 79.0f, 32.8f)
                arcToRelative(64.3f, 64.3f, 0.0f, false, true, 41.5f, 18.9f)
                lineToRelative(7.5f, 7.4f)
                lineToRelative(9.6f, -9.5f)
                arcToRelative(59.9f, 59.9f, 0.0f, false, true, 87.0f, 2.3f)
                arcTo(57.7f, 57.7f, 0.0f, false, true, 239.2f, 95.0f)
                arcToRelative(64.3f, 64.3f, 0.0f, false, true, -18.9f, 41.5f)
                lineToRelative(-81.0f, 81.0f)
                horizontalLineToRelative(0.0f)
                arcTo(15.6f, 15.6f, 0.0f, false, true, 128.0f, 222.2f)
                close()
                moveTo(75.0f, 48.7f)
                arcTo(42.5f, 42.5f, 0.0f, false, false, 46.6f, 59.2f)
                arcToRelative(44.0f, 44.0f, 0.0f, false, false, -1.7f, 63.9f)
                lineToRelative(88.8f, 88.8f)
                lineToRelative(-5.7f, -5.7f)
                lineToRelative(81.0f, -81.0f)
                curveToRelative(17.5f, -17.4f, 19.1f, -45.5f, 3.8f, -62.6f)
                arcToRelative(44.0f, 44.0f, 0.0f, false, false, -63.9f, -1.7f)
                lineTo(133.7f, 76.1f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -11.4f, 0.0f)
                lineTo(109.2f, 63.0f)
                arcTo(48.4f, 48.4f, 0.0f, false, false, 75.0f, 48.7f)
                close()
            }
        }
            .build()
        return _heart_straight!!
    }

private var _heart_straight: ImageVector? = null
