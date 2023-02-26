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

public val Phosphor.HeartStraightFill: ImageVector
    get() {
        if (_heart_straight_fill != null) {
            return _heart_straight_fill!!
        }
        _heart_straight_fill = Builder(
            name = "Heart-straight-fill",
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
                moveTo(224.6f, 51.9f)
                arcToRelative(59.5f, 59.5f, 0.0f, false, false, -43.0f, -19.9f)
                arcToRelative(60.5f, 60.5f, 0.0f, false, false, -44.0f, 17.6f)
                lineTo(128.0f, 59.1f)
                lineToRelative(-7.5f, -7.4f)
                curveTo(97.2f, 28.3f, 59.2f, 26.3f, 35.9f, 47.4f)
                arcToRelative(59.9f, 59.9f, 0.0f, false, false, -2.3f, 87.0f)
                lineToRelative(83.1f, 83.1f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 22.6f, 0.0f)
                lineToRelative(81.0f, -81.0f)
                curveTo(243.7f, 113.2f, 245.6f, 75.2f, 224.6f, 51.9f)
                close()
            }
        }
            .build()
        return _heart_straight_fill!!
    }

private var _heart_straight_fill: ImageVector? = null
