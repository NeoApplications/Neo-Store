package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.ShoppingCart: ImageVector
    get() {
        if (_ShoppingCart != null) {
            return _ShoppingCart!!
        }
        _ShoppingCart = ImageVector.Builder(
            name = "ShoppingCart",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(230.14f, 58.87f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 224f, 56f)
                lineTo(62.68f, 56f)
                lineTo(56.6f, 22.57f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48.73f, 16f)
                lineTo(24f, 16f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 16f)
                horizontalLineToRelative(18f)
                lineTo(67.56f, 172.29f)
                arcToRelative(
                    24f,
                    24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    5.33f,
                    11.27f
                )
                arcToRelative(
                    28f,
                    28f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    44.4f,
                    8.44f
                )
                horizontalLineToRelative(45.42f)
                arcTo(27.75f, 27.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, 160f, 204f)
                arcToRelative(28f, 28f, 0f, isMoreThanHalf = true, isPositiveArc = false, 28f, -28f)
                lineTo(91.17f, 176f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -7.87f,
                    -6.57f
                )
                lineTo(80.13f, 152f)
                horizontalLineToRelative(116f)
                arcToRelative(
                    24f,
                    24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    23.61f,
                    -19.71f
                )
                lineToRelative(12.16f, -66.86f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 230.14f, 58.87f)
                close()
                moveTo(104f, 204f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 104f, 204f)
                close()
                moveTo(200f, 204f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 200f, 204f)
                close()
                moveTo(204f, 129.43f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 196.1f, 136f)
                lineTo(77.22f, 136f)
                lineTo(65.59f, 72f)
                lineTo(214.41f, 72f)
                close()
            }
        }.build()

        return _ShoppingCart!!
    }

@Suppress("ObjectPropertyName")
private var _ShoppingCart: ImageVector? = null
