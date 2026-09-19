package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Checkerboard: ImageVector
    get() {
        if (_Checkerboard != null) {
            return _Checkerboard!!
        }
        _Checkerboard = ImageVector.Builder(
            name = "Checkerboard",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(208f, 32f)
                lineTo(48f, 32f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 48f)
                lineTo(32f, 208f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(208f, 224f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -16f)
                lineTo(224f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 208f, 32f)
                close()
                moveTo(195.31f, 120f)
                lineTo(136f, 60.69f)
                lineTo(136f, 48f)
                horizontalLineToRelative(12.69f)
                lineTo(208f, 107.32f)
                lineTo(208f, 120f)
                close()
                moveTo(136f, 83.31f)
                lineTo(172.69f, 120f)
                lineTo(136f, 120f)
                close()
                moveTo(208f, 84.69f)
                lineTo(171.31f, 48f)
                lineTo(208f, 48f)
                close()
                moveTo(120f, 48f)
                verticalLineToRelative(72f)
                lineTo(48f, 120f)
                lineTo(48f, 48f)
                close()
                moveTo(107.31f, 208f)
                lineTo(48f, 148.69f)
                lineTo(48f, 136f)
                lineTo(60.69f, 136f)
                lineTo(120f, 195.31f)
                lineTo(120f, 208f)
                close()
                moveTo(120f, 172.69f)
                lineTo(83.31f, 136f)
                lineTo(120f, 136f)
                close()
                moveTo(48f, 171.31f)
                lineTo(84.69f, 208f)
                lineTo(48f, 208f)
                close()
                moveTo(208f, 208f)
                lineTo(136f, 208f)
                lineTo(136f, 136f)
                horizontalLineToRelative(72f)
                verticalLineToRelative(72f)
                close()
            }
        }.build()

        return _Checkerboard!!
    }

@Suppress("ObjectPropertyName")
private var _Checkerboard: ImageVector? = null
