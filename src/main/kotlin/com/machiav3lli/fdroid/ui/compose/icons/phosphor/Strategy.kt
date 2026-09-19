package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Strategy: ImageVector
    get() {
        if (_Strategy != null) {
            return _Strategy!!
        }
        _Strategy = ImageVector.Builder(
            name = "Strategy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(76f, 152f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = true, isPositiveArc = false, 36f, 36f)
                arcTo(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = false, 76f, 152f)
                close()
                moveTo(76f, 208f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -20f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 76f, 208f)
                close()
                moveTo(42.34f, 106.34f)
                lineTo(56.69f, 92f)
                lineTo(42.34f, 77.66f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 53.66f, 66.34f)
                lineTo(68f, 80.69f)
                lineTo(82.34f, 66.34f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 93.66f, 77.66f)
                lineTo(79.31f, 92f)
                lineToRelative(14.35f, 14.34f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -11.32f,
                    11.32f
                )
                lineTo(68f, 103.31f)
                lineTo(53.66f, 117.66f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -11.32f,
                    -11.32f
                )
                close()
                moveTo(229.66f, 202.34f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -11.32f,
                    11.32f
                )
                lineTo(204f, 199.31f)
                lineToRelative(-14.34f, 14.35f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -11.32f,
                    -11.32f
                )
                lineTo(192.69f, 188f)
                lineToRelative(-14.35f, -14.34f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    11.32f,
                    -11.32f
                )
                lineTo(204f, 176.69f)
                lineToRelative(14.34f, -14.35f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    11.32f,
                    11.32f
                )
                lineTo(215.31f, 188f)
                close()
                moveTo(184.47f, 112.83f)
                curveToRelative(-6.18f, 22.33f, -25.32f, 41.63f, -46.53f, 46.93f)
                arcTo(8.13f, 8.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 136f, 160f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -1.93f,
                    -15.76f
                )
                curveToRelative(15.63f, -3.91f, 30.35f, -18.91f, 35f, -35.68f)
                curveToRelative(3.19f, -11.5f, 3.22f, -29f, -14.71f, -46.9f)
                lineTo(152f, 59.31f)
                lineTo(152f, 80f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 0f)
                lineTo(136f, 40f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, -8f)
                horizontalLineToRelative(40f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 16f)
                lineTo(163.31f, 48f)
                lineToRelative(2.35f, 2.34f)
                curveTo(183.9f, 68.59f, 190.58f, 90.78f, 184.47f, 112.83f)
                close()
            }
        }.build()

        return _Strategy!!
    }

@Suppress("ObjectPropertyName")
private var _Strategy: ImageVector? = null
