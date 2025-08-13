package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Images: ImageVector
    get() {
        if (_Images != null) {
            return _Images!!
        }
        _Images = ImageVector.Builder(
            name = "Images",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(216f, 40f)
                lineTo(72f, 40f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                lineTo(56f, 72f)
                lineTo(40f, 72f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 88f)
                lineTo(24f, 200f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(184f, 216f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -16f
                )
                lineTo(200f, 184f)
                horizontalLineToRelative(16f)
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
                moveTo(72f, 56f)
                lineTo(216f, 56f)
                verticalLineToRelative(62.75f)
                lineToRelative(-10.07f, -10.06f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -22.63f,
                    0f
                )
                lineToRelative(-20f, 20f)
                lineToRelative(-44f, -44f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -22.62f,
                    0f
                )
                lineTo(72f, 109.37f)
                close()
                moveTo(184f, 200f)
                lineTo(40f, 200f)
                lineTo(40f, 88f)
                lineTo(56f, 88f)
                verticalLineToRelative(80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(184f, 184f)
                close()
                moveTo(216f, 168f)
                lineTo(72f, 168f)
                lineTo(72f, 132f)
                lineToRelative(36f, -36f)
                lineToRelative(49.66f, 49.66f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11.31f, 0f)
                lineTo(194.63f, 120f)
                lineTo(216f, 141.38f)
                lineTo(216f, 168f)
                close()
                moveTo(160f, 84f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 84f)
                close()
            }
        }.build()

        return _Images!!
    }

@Suppress("ObjectPropertyName")
private var _Images: ImageVector? = null
