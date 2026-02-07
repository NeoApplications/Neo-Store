package com.machiav3lli.fdroid.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Icon

val Icon.IcVirustotal: ImageVector
    get() {
        if (_Virustotal != null) {
            return _Virustotal!!
        }
        _Virustotal = ImageVector.Builder(
            name = "IcVirustotal",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(46f, 50f)
                lineTo(7.5f, 87.82f)
                horizontalLineToRelative(85f)
                verticalLineTo(12.17f)
                horizontalLineTo(7.5f)
                close()
                moveTo(86f, 82.17f)
                horizontalLineTo(24.2f)
                lineToRelative(32.59f, -31.92f)
                lineToRelative(-32.59f, -32.43f)
                horizontalLineToRelative(61.8f)
                close()
            }
        }.build()

        return _Virustotal!!
    }

@Suppress("ObjectPropertyName")
private var _Virustotal: ImageVector? = null