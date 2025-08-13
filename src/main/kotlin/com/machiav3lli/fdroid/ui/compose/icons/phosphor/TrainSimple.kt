package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.TrainSimple: ImageVector
    get() {
        if (_TrainSimple != null) {
            return _TrainSimple!!
        }
        _TrainSimple = ImageVector.Builder(
            name = "TrainSimple",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(184f, 24f)
                lineTo(72f, 24f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 40f, 56f)
                lineTo(40f, 184f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 32f)
                horizontalLineToRelative(8f)
                lineTo(65.6f, 235.2f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12.8f, 9.6f)
                lineTo(100f, 216f)
                horizontalLineToRelative(56f)
                lineToRelative(21.6f, 28.8f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    12.8f,
                    -9.6f
                )
                lineTo(176f, 216f)
                horizontalLineToRelative(8f)
                arcToRelative(
                    32f,
                    32f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    32f,
                    -32f
                )
                lineTo(216f, 56f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 184f, 24f)
                close()
                moveTo(72f, 40f)
                lineTo(184f, 40f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 16f)
                verticalLineToRelative(64f)
                lineTo(56f, 120f)
                lineTo(56f, 56f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72f, 40f)
                close()
                moveTo(184f, 200f)
                lineTo(72f, 200f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -16f,
                    -16f
                )
                lineTo(56f, 136f)
                lineTo(200f, 136f)
                verticalLineToRelative(48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 184f, 200f)
                close()
                moveTo(96f, 172f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96f, 172f)
                close()
                moveTo(184f, 172f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 184f, 172f)
                close()
            }
        }.build()

        return _TrainSimple!!
    }

@Suppress("ObjectPropertyName")
private var _TrainSimple: ImageVector? = null
