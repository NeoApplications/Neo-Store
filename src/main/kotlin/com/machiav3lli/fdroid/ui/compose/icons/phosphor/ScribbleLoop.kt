package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.ScribbleLoop: ImageVector
    get() {
        if (_ScribbleLoop != null) {
            return _ScribbleLoop!!
        }
        _ScribbleLoop = ImageVector.Builder(
            name = "ScribbleLoop",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(253.93f, 154.63f)
                curveToRelative(-1.32f, -1.46f, -24.09f, -26.22f, -61f, -40.56f)
                curveToRelative(-1.72f, -18.42f, -8.46f, -35.17f, -19.41f, -47.92f)
                curveTo(158.87f, 49f, 137.58f, 40f, 112f, 40f)
                curveTo(60.48f, 40f, 26.89f, 86.18f, 25.49f, 88.15f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13f, 9.31f)
                curveTo(38.8f, 97.05f, 68.81f, 56f, 112f, 56f)
                curveToRelative(20.77f, 0f, 37.86f, 7.11f, 49.41f, 20.57f)
                curveToRelative(7.42f, 8.64f, 12.44f, 19.69f, 14.67f, 32f)
                arcTo(
                    140.87f,
                    140.87f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    140.6f,
                    104f
                )
                curveToRelative(-26.06f, 0f, -47.93f, 6.81f, -63.26f, 19.69f)
                curveTo(63.78f, 135.09f, 56f, 151f, 56f, 167.25f)
                arcTo(
                    47.59f,
                    47.59f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    69.87f,
                    201.3f
                )
                curveToRelative(9.66f, 9.62f, 23.06f, 14.7f, 38.73f, 14.7f)
                curveToRelative(51.81f, 0f, 81.18f, -42.13f, 84.49f, -84.42f)
                arcToRelative(
                    161.43f,
                    161.43f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    49f,
                    33.79f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    11.86f,
                    -10.74f
                )
                close()
                moveTo(159.47f, 176.27f)
                curveTo(150.64f, 187.09f, 134.66f, 200f, 108.6f, 200f)
                curveTo(83.32f, 200f, 72f, 183.55f, 72f, 167.25f)
                curveTo(72f, 144.49f, 93.47f, 120f, 140.6f, 120f)
                arcToRelative(
                    124.34f,
                    124.34f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    36.78f,
                    5.68f
                )
                curveTo(176.93f, 144.44f, 170.46f, 162.78f, 159.47f, 176.27f)
                close()
            }
        }.build()

        return _ScribbleLoop!!
    }

@Suppress("ObjectPropertyName")
private var _ScribbleLoop: ImageVector? = null
