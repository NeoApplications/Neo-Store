package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.PuzzlePiece: ImageVector
    get() {
        if (_PuzzlePiece != null) {
            return _PuzzlePiece!!
        }
        _PuzzlePiece = ImageVector.Builder(
            name = "PuzzlePiece",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(220.27f, 158.54f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -7.7f,
                    -0.46f
                )
                arcToRelative(
                    20f,
                    20f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    0f,
                    -36.16f
                )
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 224f, 114.69f)
                verticalLineTo(72f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -16f,
                    -16f
                )
                horizontalLineTo(171.78f)
                arcToRelative(
                    35.36f,
                    35.36f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.22f,
                    -4f
                )
                arcToRelative(
                    36.11f,
                    36.11f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.36f,
                    -26.24f
                )
                arcToRelative(
                    36f,
                    36f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -60.55f,
                    23.62f
                )
                arcToRelative(
                    36.56f,
                    36.56f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.14f,
                    6.62f
                )
                horizontalLineTo(64f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 72f)
                verticalLineToRelative(32.22f)
                arcToRelative(
                    35.36f,
                    35.36f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -4f,
                    -0.22f
                )
                arcToRelative(
                    36.12f,
                    36.12f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -26.24f,
                    11.36f
                )
                arcToRelative(
                    35.7f,
                    35.7f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -9.69f,
                    27f
                )
                arcToRelative(
                    36.08f,
                    36.08f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    33.31f,
                    33.6f
                )
                arcToRelative(
                    35.68f,
                    35.68f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    6.62f,
                    -0.14f
                )
                verticalLineTo(208f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                horizontalLineTo(208f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -16f
                )
                verticalLineTo(165.31f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 220.27f, 158.54f)
                close()
                moveTo(208f, 208f)
                horizontalLineTo(64f)
                verticalLineTo(165.31f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.43f,
                    -7.23f
                )
                arcToRelative(
                    20f,
                    20f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    0f,
                    -36.16f
                )
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 114.69f)
                verticalLineTo(72f)
                horizontalLineToRelative(46.69f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    7.23f,
                    -11.43f
                )
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 36.16f, 0f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 161.31f, 72f)
                horizontalLineTo(208f)
                verticalLineToRelative(32.23f)
                arcToRelative(
                    35.68f,
                    35.68f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -6.62f,
                    -0.14f
                )
                arcTo(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = false, 204f, 176f)
                arcToRelative(
                    35.36f,
                    35.36f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    4f,
                    -0.22f
                )
                close()
            }
        }.build()

        return _PuzzlePiece!!
    }

@Suppress("ObjectPropertyName")
private var _PuzzlePiece: ImageVector? = null
