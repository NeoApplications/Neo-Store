package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.SoccerBall: ImageVector
    get() {
        if (_SoccerBall != null) {
            return _SoccerBall!!
        }
        _SoccerBall = ImageVector.Builder(
            name = "SoccerBall",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(128f, 24f)
                arcTo(104f, 104f, 0f, isMoreThanHalf = true, isPositiveArc = false, 232f, 128f)
                arcTo(
                    104.11f,
                    104.11f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    128f,
                    24f
                )
                close()
                moveTo(204.52f, 171.42f)
                lineTo(170.9f, 171.42f)
                lineToRelative(-9.26f, -12.76f)
                lineToRelative(12.63f, -36.78f)
                lineToRelative(15f, -4.89f)
                lineToRelative(26.24f, 20.13f)
                arcTo(
                    87.38f,
                    87.38f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    204.52f,
                    171.42f
                )
                close()
                moveTo(40.52f, 137.12f)
                lineTo(66.71f, 117f)
                lineToRelative(15f, 4.89f)
                lineToRelative(12.63f, 36.78f)
                lineTo(85.1f, 171.42f)
                lineTo(51.48f, 171.42f)
                arcTo(
                    87.38f,
                    87.38f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    40.47f,
                    137.12f
                )
                close()
                moveTo(50.52f, 86.48f)
                lineTo(56.03f, 105.08f)
                lineTo(40.71f, 116.77f)
                arcTo(
                    87.33f,
                    87.33f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    50.43f,
                    86.48f
                )
                close()
                moveTo(109f, 152f)
                lineTo(97.54f, 118.65f)
                lineTo(128f, 97.71f)
                lineToRelative(30.46f, 20.94f)
                lineTo(147f, 152f)
                close()
                moveTo(200.07f, 105.08f)
                lineTo(205.58f, 86.48f)
                arcToRelative(
                    87.33f,
                    87.33f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    9.72f,
                    30.29f
                )
                close()
                moveTo(193.87f, 69.7f)
                lineTo(184.36f, 101.78f)
                lineTo(169.29f, 106.67f)
                lineTo(136f, 83.79f)
                lineTo(136f, 68.21f)
                lineToRelative(29.09f, -20f)
                arcTo(
                    88.58f,
                    88.58f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    193.86f,
                    69.7f
                )
                close()
                moveTo(146.07f, 41.87f)
                lineTo(128f, 54.29f)
                lineTo(109.93f, 41.87f)
                arcToRelative(
                    88.24f,
                    88.24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    36.14f,
                    0f
                )
                close()
                moveTo(90.91f, 48.21f)
                lineToRelative(29.09f, 20f)
                lineTo(120f, 83.79f)
                lineTo(86.72f, 106.67f)
                lineToRelative(-15.07f, -4.89f)
                lineTo(62.14f, 69.7f)
                arcTo(
                    88.58f,
                    88.58f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    90.91f,
                    48.21f
                )
                close()
                moveTo(63.15f, 187.42f)
                lineTo(83.52f, 187.42f)
                lineToRelative(7.17f, 20.27f)
                arcTo(
                    88.4f,
                    88.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    63.15f,
                    187.42f
                )
                close()
                moveTo(110f, 214.13f)
                lineTo(98.12f, 180.71f)
                lineTo(107.35f, 168f)
                horizontalLineToRelative(41.3f)
                lineToRelative(9.23f, 12.71f)
                lineToRelative(-11.83f, 33.42f)
                arcToRelative(
                    88f,
                    88f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -36.1f,
                    0f
                )
                close()
                moveTo(165.36f, 207.69f)
                lineTo(172.53f, 187.42f)
                horizontalLineToRelative(20.37f)
                arcTo(
                    88.4f,
                    88.4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    165.31f,
                    207.69f
                )
                close()
            }
        }.build()

        return _SoccerBall!!
    }

@Suppress("ObjectPropertyName")
private var _SoccerBall: ImageVector? = null
