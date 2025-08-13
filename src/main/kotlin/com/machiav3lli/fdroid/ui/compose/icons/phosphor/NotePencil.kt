package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.NotePencil: ImageVector
    get() {
        if (_NotePencil != null) {
            return _NotePencil!!
        }
        _NotePencil = ImageVector.Builder(
            name = "NotePencil",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(229.66f, 58.34f)
                lineToRelative(-32f, -32f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.32f,
                    0f
                )
                lineToRelative(-96f, 96f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 88f, 128f)
                verticalLineToRelative(32f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                horizontalLineToRelative(32f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    5.66f,
                    -2.34f
                )
                lineToRelative(96f, -96f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 229.66f, 58.34f)
                close()
                moveTo(124.69f, 152f)
                horizontalLineTo(104f)
                verticalLineTo(131.31f)
                lineToRelative(64f, -64f)
                lineTo(188.69f, 88f)
                close()
                moveTo(200f, 76.69f)
                lineTo(179.31f, 56f)
                lineTo(192f, 43.31f)
                lineTo(212.69f, 64f)
                close()
                moveTo(224f, 128f)
                verticalLineToRelative(80f)
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
                horizontalLineToRelative(80f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 16f)
                horizontalLineTo(48f)
                verticalLineTo(208f)
                horizontalLineTo(208f)
                verticalLineTo(128f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 0f)
                close()
            }
        }.build()

        return _NotePencil!!
    }

@Suppress("ObjectPropertyName")
private var _NotePencil: ImageVector? = null
