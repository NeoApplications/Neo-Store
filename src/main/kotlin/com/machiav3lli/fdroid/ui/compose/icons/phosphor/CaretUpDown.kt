package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.CaretUpDown: ImageVector
    get() {
        if (_CaretUpDown != null) {
            return _CaretUpDown!!
        }
        _CaretUpDown = ImageVector.Builder(
            name = "CaretUpDown",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(181.66f, 170.34f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 11.32f)
                lineToRelative(-48f, 48f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.32f, 0f)
                lineToRelative(-48f, -48f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    11.32f,
                    -11.32f
                )
                lineTo(128f, 212.69f)
                lineToRelative(42.34f, -42.35f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 181.66f, 170.34f)
                close()
                moveTo(85.66f, 85.66f)
                lineTo(128f, 43.31f)
                lineToRelative(42.34f, 42.35f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    11.32f,
                    -11.32f
                )
                lineToRelative(-48f, -48f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.32f,
                    0f
                )
                lineToRelative(-48f, 48f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 85.66f, 85.66f)
                close()
            }
        }.build()

        return _CaretUpDown!!
    }

@Suppress("ObjectPropertyName")
private var _CaretUpDown: ImageVector? = null
