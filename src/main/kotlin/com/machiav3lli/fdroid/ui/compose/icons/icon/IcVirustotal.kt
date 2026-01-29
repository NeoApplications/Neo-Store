package com.machiav3lli.fdroid.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Icon

val Icon.IcVirustotal: ImageVector
    get() {
        if (_IcVirustotal != null) {
            return _IcVirustotal!!
        }
        _IcVirustotal = ImageVector.Builder(
            name = "Virustotal",
            viewportWidth = 100f,
            viewportHeight = 100f,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(45.29f, 44.5f)
                lineTo(0f, 89f)
                horizontalLineToRelative(100f)
                verticalLineTo(0f)
                horizontalLineTo(0f)
                lineToRelative(45.29f, 44.5f)
                close()
                moveTo(90f, 80f)
                horizontalLineTo(22f)
                lineToRelative(35.99f, -35.2f)
                lineTo(22f, 9f)
                horizontalLineToRelative(68f)
                verticalLineToRelative(71f)
                close()
            }
        }.build()

        return _IcVirustotal!!
    }

@Suppress("ObjectPropertyName")
private var _IcVirustotal: ImageVector? = null