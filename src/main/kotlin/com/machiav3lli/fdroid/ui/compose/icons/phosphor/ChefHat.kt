package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.ChefHat: ImageVector
    get() {
        if (_ChefHat != null) {
            return _ChefHat!!
        }
        _ChefHat = ImageVector.Builder(
            name = "ChefHat",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(240f, 112f)
                arcToRelative(
                    56.06f,
                    56.06f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -56f,
                    -56f
                )
                curveToRelative(-1.77f, 0f, -3.54f, 0.1f, -5.29f, 0.26f)
                arcToRelative(
                    56f,
                    56f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -101.42f,
                    0f
                )
                curveTo(75.54f, 56.1f, 73.77f, 56f, 72f, 56f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 162.59f)
                lineTo(48f, 208f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(192f, 224f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -16f
                )
                lineTo(208f, 162.59f)
                arcTo(56.09f, 56.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240f, 112f)
                close()
                moveTo(192f, 208f)
                lineTo(64f, 208f)
                lineTo(64f, 167.42f)
                arcToRelative(
                    55.49f,
                    55.49f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    8f,
                    0.58f
                )
                lineTo(184f, 168f)
                arcToRelative(
                    55.49f,
                    55.49f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    8f,
                    -0.58f
                )
                close()
                moveTo(184f, 152f)
                lineTo(170.25f, 152f)
                lineToRelative(5.51f, -22.06f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -15.52f,
                    -3.88f
                )
                lineTo(153.75f, 152f)
                lineTo(136f, 152f)
                lineTo(136f, 128f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 0f)
                verticalLineToRelative(24f)
                lineTo(102.25f, 152f)
                lineToRelative(-6.49f, -25.94f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -15.52f,
                    3.88f
                )
                lineTo(85.75f, 152f)
                lineTo(72f, 152f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -80f)
                lineToRelative(0.58f, 0f)
                arcTo(55.21f, 55.21f, 0f, isMoreThanHalf = false, isPositiveArc = false, 72f, 80f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 0f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 80f, 0f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 0f)
                arcToRelative(
                    55.21f,
                    55.21f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.58f,
                    -8f
                )
                lineToRelative(0.58f, 0f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 80f)
                close()
            }
        }.build()

        return _ChefHat!!
    }

@Suppress("ObjectPropertyName")
private var _ChefHat: ImageVector? = null
