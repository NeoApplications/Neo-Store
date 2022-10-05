package com.machiav3lli.fdroid.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Icon

val Icon.IcDonateLitecoin: ImageVector
    get() {
        if (_icDonateLitecoin != null) {
            return _icDonateLitecoin!!
        }
        _icDonateLitecoin = Builder(
            name = "IcDonateLitecoin",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(11.065f, 16.274f)
                lineToRelative(1.039f, -3.913f)
                lineToRelative(2.46f, -0.899f)
                lineToRelative(0.612f, -2.3f)
                lineToRelative(-0.021f, -0.057f)
                lineToRelative(-2.422f, 0.885f)
                lineToRelative(1.745f, -6.571f)
                horizontalLineTo(9.53f)
                lineTo(7.248f, 11.994f)
                lineTo(5.342f, 12.69f)
                lineTo(4.713f, 15.061f)
                lineTo(6.617f, 14.366f)
                lineTo(5.272f, 19.419f)
                horizontalLineTo(18.443f)
                lineToRelative(0.844f, -3.145f)
                horizontalLineToRelative(-8.222f)
            }
        }
            .build()
        return _icDonateLitecoin!!
    }

private var _icDonateLitecoin: ImageVector? = null
