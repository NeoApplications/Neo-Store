package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Ghost: ImageVector
    get() {
        if (_Ghost != null) {
            return _Ghost!!
        }
        _Ghost = ImageVector.Builder(
            name = "Ghost",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(112f, 116f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 112f, 116f)
                close()
                moveTo(156f, 104f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 156f, 104f)
                close()
                moveTo(224f, 120f)
                verticalLineToRelative(96f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -13.07f,
                    6.19f
                )
                lineToRelative(-24.26f, -19.85f)
                lineTo(162.4f, 222.19f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -10.13f, 0f)
                lineTo(128f, 202.34f)
                lineToRelative(-24.27f, 19.85f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -10.13f, 0f)
                lineTo(69.33f, 202.34f)
                lineTo(45.07f, 222.19f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 216f)
                lineTo(32f, 120f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192f, 0f)
                close()
                moveTo(208f, 120f)
                arcToRelative(
                    80f,
                    80f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -160f,
                    0f
                )
                verticalLineToRelative(79.12f)
                lineToRelative(16.27f, -13.31f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.13f, 0f)
                lineToRelative(24.27f, 19.85f)
                lineToRelative(24.26f, -19.85f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.14f, 0f)
                lineToRelative(24.26f, 19.85f)
                lineToRelative(24.27f, -19.85f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.13f, 0f)
                lineTo(208f, 199.12f)
                close()
            }
        }.build()

        return _Ghost!!
    }

@Suppress("ObjectPropertyName")
private var _Ghost: ImageVector? = null
