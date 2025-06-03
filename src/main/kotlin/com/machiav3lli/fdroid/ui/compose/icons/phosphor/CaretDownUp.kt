package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.CaretDownUp: ImageVector
    get() {
        if (_CaretDownUp != null) {
            return _CaretDownUp!!
        }
        _CaretDownUp = ImageVector.Builder(
            name = "CaretDownUp",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveToRelative(85.66f, 26.34f)
                lineToRelative(42.34f, 42.35f)
                lineToRelative(42.34f, -42.35f)
                curveToRelative(3.13f, -3.13f, 8.19f, -3.13f, 11.32f, 0f)
                curveToRelative(3.13f, 3.13f, 3.13f, 8.19f, 0f, 11.32f)
                lineToRelative(-48f, 48f)
                curveToRelative(-3.12f, 3.13f, -8.2f, 3.13f, -11.32f, 0f)
                lineToRelative(-48f, -48f)
                curveToRelative(-3.13f, -3.13f, -3.13f, -8.19f, 0f, -11.32f)
                curveToRelative(3.13f, -3.13f, 8.19f, -3.13f, 11.32f, -0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 0.999988f
            ) {
                moveToRelative(181.66f, 229.66f)
                curveToRelative(3.13f, -3.12f, 3.13f, -8.2f, 0f, -11.32f)
                lineToRelative(-48f, -48f)
                curveToRelative(-3.12f, -3.13f, -8.19f, -3.13f, -11.32f, 0f)
                lineToRelative(-48f, 48f)
                curveToRelative(-3.13f, 3.13f, -3.13f, 8.19f, 0f, 11.32f)
                curveToRelative(3.13f, 3.13f, 8.19f, 3.13f, 11.32f, 0f)
                lineToRelative(42.34f, -42.35f)
                lineToRelative(42.34f, 42.35f)
                curveToRelative(3.12f, 3.13f, 8.19f, 3.13f, 11.32f, 0f)
                close()
            }
        }.build()

        return _CaretDownUp!!
    }

@Suppress("ObjectPropertyName")
private var _CaretDownUp: ImageVector? = null
