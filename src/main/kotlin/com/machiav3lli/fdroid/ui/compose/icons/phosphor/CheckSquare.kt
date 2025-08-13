package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.CheckSquare: ImageVector
    get() {
        if (_CheckSquare != null) {
            return _CheckSquare!!
        }
        _CheckSquare = ImageVector.Builder(
            name = "CheckSquare",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(173.66f, 98.34f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 11.32f)
                lineToRelative(-56f, 56f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.32f, 0f)
                lineToRelative(-24f, -24f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    11.32f,
                    -11.32f
                )
                lineTo(112f, 148.69f)
                lineToRelative(50.34f, -50.35f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 173.66f, 98.34f)
                close()
                moveTo(224f, 48f)
                verticalLineTo(208f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 16f)
                horizontalLineTo(48f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -16f,
                    -16f
                )
                verticalLineTo(48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 32f)
                horizontalLineTo(208f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 48f)
                close()
                moveTo(208f, 208f)
                verticalLineTo(48f)
                horizontalLineTo(48f)
                verticalLineTo(208f)
                horizontalLineTo(208f)
                close()
            }
        }.build()

        return _CheckSquare!!
    }

@Suppress("ObjectPropertyName")
private var _CheckSquare: ImageVector? = null
