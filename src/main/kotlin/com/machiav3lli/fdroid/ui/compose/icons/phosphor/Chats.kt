package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Chats: ImageVector
    get() {
        if (_Chats != null) {
            return _Chats!!
        }
        _Chats = ImageVector.Builder(
            name = "Chats",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(216f, 80f)
                horizontalLineTo(184f)
                verticalLineTo(48f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -16f,
                    -16f
                )
                horizontalLineTo(40f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 48f)
                verticalLineTo(176f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13f, 6.22f)
                lineTo(72f, 154f)
                verticalLineTo(184f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                horizontalLineToRelative(93.59f)
                lineTo(219f, 230.22f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5f, 1.78f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                verticalLineTo(96f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 216f, 80f)
                close()
                moveTo(66.55f, 137.78f)
                lineTo(40f, 159.25f)
                verticalLineTo(48f)
                horizontalLineTo(168f)
                verticalLineToRelative(88f)
                horizontalLineTo(71.58f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 66.55f, 137.78f)
                close()
                moveTo(216f, 207.25f)
                lineToRelative(-26.55f, -21.47f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -5f,
                    -1.78f
                )
                horizontalLineTo(88f)
                verticalLineTo(152f)
                horizontalLineToRelative(80f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -16f
                )
                verticalLineTo(96f)
                horizontalLineToRelative(32f)
                close()
            }
        }.build()

        return _Chats!!
    }

@Suppress("ObjectPropertyName")
private var _Chats: ImageVector? = null