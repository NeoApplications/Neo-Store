package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Browser: ImageVector
    get() {
        if (_Browser != null) {
            return _Browser!!
        }
        _Browser = ImageVector.Builder(
            name = "Browser",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(216f, 40f)
                lineTo(40f, 40f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 56f)
                lineTo(24f, 200f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(216f, 216f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -16f
                )
                lineTo(232f, 56f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 216f, 40f)
                close()
                moveTo(216f, 56f)
                lineTo(216f, 88f)
                lineTo(40f, 88f)
                lineTo(40f, 56f)
                close()
                moveTo(216f, 200f)
                lineTo(40f, 200f)
                lineTo(40f, 104f)
                lineTo(216f, 104f)
                verticalLineToRelative(96f)
                close()
            }
        }.build()

        return _Browser!!
    }

@Suppress("ObjectPropertyName")
private var _Browser: ImageVector? = null
