package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Translate: ImageVector
    get() {
        if (_Translate != null) {
            return _Translate!!
        }
        _Translate = ImageVector.Builder(
            name = "Translate",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(247.15f, 212.42f)
                lineToRelative(-56f, -112f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -14.31f,
                    0f
                )
                lineToRelative(-21.71f, 43.43f)
                arcTo(88f, 88f, 0f, isMoreThanHalf = false, isPositiveArc = true, 108f, 126.93f)
                arcTo(
                    103.65f,
                    103.65f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    135.69f,
                    64f
                )
                horizontalLineTo(160f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -16f)
                horizontalLineTo(104f)
                verticalLineTo(32f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 0f)
                verticalLineTo(48f)
                horizontalLineTo(32f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 16f)
                horizontalLineToRelative(87.63f)
                arcTo(
                    87.76f,
                    87.76f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    96f,
                    116.35f
                )
                arcToRelative(
                    87.74f,
                    87.74f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -19f,
                    -31f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -15.08f,
                    5.34f
                )
                arcTo(
                    103.63f,
                    103.63f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    84f,
                    127f
                )
                arcToRelative(
                    87.55f,
                    87.55f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -52f,
                    17f
                )
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 16f)
                arcToRelative(
                    103.46f,
                    103.46f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    64f,
                    -22.08f
                )
                arcToRelative(
                    104.18f,
                    104.18f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    51.44f,
                    21.31f
                )
                lineToRelative(-26.6f, 53.19f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    14.31f,
                    7.16f
                )
                lineTo(148.94f, 192f)
                horizontalLineToRelative(70.11f)
                lineToRelative(13.79f, 27.58f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240f, 224f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    7.15f,
                    -11.58f
                )
                close()
                moveTo(156.94f, 176f)
                lineTo(184f, 121.89f)
                lineTo(211.05f, 176f)
                close()
            }
        }.build()

        return _Translate!!
    }

@Suppress("ObjectPropertyName")
private var _Translate: ImageVector? = null
