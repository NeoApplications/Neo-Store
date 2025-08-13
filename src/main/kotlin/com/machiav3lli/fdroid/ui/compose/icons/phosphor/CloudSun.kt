package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.CloudSun: ImageVector
    get() {
        if (_CloudSun != null) {
            return _CloudSun!!
        }
        _CloudSun = ImageVector.Builder(
            name = "CloudSun",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(164f, 72f)
                arcToRelative(
                    76.2f,
                    76.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -20.26f,
                    2.73f
                )
                arcToRelative(
                    55.63f,
                    55.63f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -9.41f,
                    -11.54f
                )
                lineToRelative(9.51f, -13.57f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -13.11f,
                    -9.18f
                )
                lineTo(121.22f, 54f)
                arcTo(55.9f, 55.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, 48f)
                curveToRelative(-0.58f, 0f, -1.16f, 0f, -1.74f, 0f)
                lineTo(91.37f, 31.71f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -15.75f,
                    2.77f
                )
                lineTo(78.5f, 50.82f)
                arcTo(
                    56.1f,
                    56.1f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    55.23f,
                    65.67f
                )
                lineTo(41.61f, 56.14f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -9.17f,
                    13.11f
                )
                lineTo(46f, 78.77f)
                arcTo(55.55f, 55.55f, 0f, isMoreThanHalf = false, isPositiveArc = false, 40f, 104f)
                curveToRelative(0f, 0.57f, 0f, 1.15f, 0f, 1.72f)
                lineTo(23.71f, 108.6f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.38f,
                    15.88f
                )
                arcToRelative(
                    8.24f,
                    8.24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.39f,
                    -0.12f
                )
                lineToRelative(16.32f, -2.88f)
                arcToRelative(
                    55.74f,
                    55.74f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    5.86f,
                    12.42f
                )
                arcTo(52f, 52f, 0f, isMoreThanHalf = false, isPositiveArc = false, 84f, 224f)
                horizontalLineToRelative(80f)
                arcToRelative(
                    76f,
                    76f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -152f
                )
                close()
                moveTo(56f, 104f)
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    72.54f,
                    -23.24f
                )
                arcToRelative(
                    76.26f,
                    76.26f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -35.62f,
                    40f
                )
                arcToRelative(
                    52.14f,
                    52.14f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -31f,
                    4.17f
                )
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 56f, 104f)
                close()
                moveTo(164f, 208f)
                horizontalLineTo(84f)
                arcToRelative(
                    36f,
                    36f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    4.78f,
                    -71.69f
                )
                curveToRelative(-0.37f, 2.37f, -0.63f, 4.79f, -0.77f, 7.23f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 0.92f)
                arcToRelative(
                    58.91f,
                    58.91f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    1.88f,
                    -11.81f
                )
                curveToRelative(0f, -0.16f, 0.09f, -0.32f, 0.12f, -0.48f)
                arcTo(60.06f, 60.06f, 0f, isMoreThanHalf = true, isPositiveArc = true, 164f, 208f)
                close()
            }
        }.build()

        return _CloudSun!!
    }

@Suppress("ObjectPropertyName")
private var _CloudSun: ImageVector? = null
