package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Storefront: ImageVector
    get() {
        if (_Storefront != null) {
            return _Storefront!!
        }
        _Storefront = ImageVector.Builder(
            name = "Storefront",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(232f, 96f)
                arcToRelative(
                    7.89f,
                    7.89f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.3f,
                    -2.2f
                )
                lineTo(217.35f, 43.6f)
                arcTo(16.07f, 16.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 202f, 32f)
                lineTo(54f, 32f)
                arcTo(
                    16.07f,
                    16.07f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    38.65f,
                    43.6f
                )
                lineTo(24.31f, 93.8f)
                arcTo(7.89f, 7.89f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 96f)
                horizontalLineToRelative(0f)
                verticalLineToRelative(16f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 32f)
                verticalLineToRelative(72f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                lineTo(208f, 224f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                lineTo(216f, 144f)
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -32f
                )
                lineTo(232f, 96f)
                close()
                moveTo(54f, 48f)
                lineTo(202f, 48f)
                lineToRelative(11.42f, 40f)
                lineTo(42.61f, 88f)
                close()
                moveTo(104f, 104f)
                horizontalLineToRelative(48f)
                verticalLineToRelative(8f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -48f, 0f)
                close()
                moveTo(88f, 104f)
                verticalLineToRelative(8f)
                arcToRelative(
                    24f,
                    24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -35.12f,
                    21.26f
                )
                arcToRelative(
                    7.88f,
                    7.88f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.82f,
                    -1.06f
                )
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, 112f)
                verticalLineToRelative(-8f)
                close()
                moveTo(200f, 208f)
                lineTo(56f, 208f)
                lineTo(56f, 151.2f)
                arcToRelative(
                    40.57f,
                    40.57f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    8f,
                    0.8f
                )
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    32f,
                    -16f
                )
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 0f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 16f)
                arcToRelative(
                    40.57f,
                    40.57f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    8f,
                    -0.8f
                )
                close()
                moveTo(204.93f, 132.2f)
                arcToRelative(
                    8.08f,
                    8.08f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.8f,
                    1.05f
                )
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 168f, 112f)
                verticalLineToRelative(-8f)
                horizontalLineToRelative(48f)
                verticalLineToRelative(8f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 204.93f, 132.2f)
                close()
            }
        }.build()

        return _Storefront!!
    }

@Suppress("ObjectPropertyName")
private var _Storefront: ImageVector? = null
