package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Flask: ImageVector
    get() {
        if (_flask != null) {
            return _flask!!
        }
        _flask = ImageVector.Builder(
            name = "Flask",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(221.69f, 199.77f)
                lineTo(160f, 96.92f)
                verticalLineTo(40f)
                horizontalLineToRelative(8f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -16f)
                horizontalLineTo(88f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 16f)
                horizontalLineToRelative(8f)
                verticalLineTo(96.92f)
                lineTo(34.31f, 199.77f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 224f)
                horizontalLineTo(208f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    13.72f,
                    -24.23f
                )
                close()
                moveTo(110.86f, 103.25f)
                arcTo(7.93f, 7.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 112f, 99.14f)
                verticalLineTo(40f)
                horizontalLineToRelative(32f)
                verticalLineTo(99.14f)
                arcToRelative(
                    7.93f,
                    7.93f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.14f,
                    4.11f
                )
                lineTo(183.36f, 167f)
                curveToRelative(-12f, 2.37f, -29.07f, 1.37f, -51.75f, -10.11f)
                curveToRelative(-15.91f, -8.05f, -31.05f, -12.32f, -45.22f, -12.81f)
                close()
                moveTo(48f, 208f)
                lineToRelative(28.54f, -47.58f)
                curveToRelative(14.25f, -1.74f, 30.31f, 1.85f, 47.82f, 10.72f)
                curveToRelative(19f, 9.61f, 35f, 12.88f, 48f, 12.88f)
                arcToRelative(
                    69.89f,
                    69.89f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    19.55f,
                    -2.7f
                )
                lineTo(208f, 208f)
                close()
            }
        }.build()

        return _flask!!
    }

@Suppress("ObjectPropertyName")
private var _flask: ImageVector? = null
