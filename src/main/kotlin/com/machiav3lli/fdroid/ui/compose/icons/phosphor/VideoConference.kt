package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.VideoConference: ImageVector
    get() {
        if (_VideoConference != null) {
            return _VideoConference!!
        }
        _VideoConference = ImageVector.Builder(
            name = "VideoConference",
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
                moveTo(216f, 120f)
                lineTo(168f, 120f)
                lineTo(168f, 56f)
                horizontalLineToRelative(48f)
                close()
                moveTo(40f, 56f)
                lineTo(152f, 56f)
                lineTo(152f, 200f)
                lineTo(40f, 200f)
                close()
                moveTo(216f, 200f)
                lineTo(168f, 200f)
                lineTo(168f, 136f)
                horizontalLineToRelative(48f)
                verticalLineToRelative(64f)
                close()
                moveTo(180f, 88f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 180f, 88f)
                close()
                moveTo(204f, 168f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 204f, 168f)
                close()
                moveTo(135.75f, 166f)
                arcToRelative(
                    39.76f,
                    39.76f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -17.19f,
                    -23.34f
                )
                arcToRelative(
                    32f,
                    32f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -45.12f,
                    0f
                )
                arcTo(
                    39.84f,
                    39.84f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    56.25f,
                    166f
                )
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.5f, 4f)
                curveToRelative(2.64f, -10.25f, 13.06f, -18f, 24.25f, -18f)
                reflectiveCurveToRelative(21.62f, 7.73f, 24.25f, 18f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = false, 15.5f, -4f)
                close()
                moveTo(80f, 120f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 16f, 16f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 80f, 120f)
                close()
            }
        }.build()

        return _VideoConference!!
    }

@Suppress("ObjectPropertyName")
private var _VideoConference: ImageVector? = null
