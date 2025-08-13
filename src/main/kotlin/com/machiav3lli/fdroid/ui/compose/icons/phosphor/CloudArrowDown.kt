package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.CloudArrowDown: ImageVector
    get() {
        if (_CloudArrowDown != null) {
            return _CloudArrowDown!!
        }
        _CloudArrowDown = ImageVector.Builder(
            name = "CloudArrowDown",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(248f, 128f)
                arcToRelative(
                    87.34f,
                    87.34f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -17.6f,
                    52.81f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -12.8f,
                    -9.62f
                )
                arcTo(71.34f, 71.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, 232f, 128f)
                arcToRelative(
                    72f,
                    72f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -144f,
                    0f
                )
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 0f)
                arcToRelative(
                    88f,
                    88f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    3.29f,
                    -23.88f
                )
                curveTo(74.2f, 104f, 73.1f, 104f, 72f, 104f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 96f)
                lineTo(96f, 200f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 16f)
                lineTo(72f, 216f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 81.29f, 88.68f)
                arcTo(88f, 88f, 0f, isMoreThanHalf = false, isPositiveArc = true, 248f, 128f)
                close()
                moveTo(178.34f, 170.34f)
                lineTo(160f, 188.69f)
                lineTo(160f, 128f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 0f)
                verticalLineToRelative(60.69f)
                lineToRelative(-18.34f, -18.35f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.32f,
                    11.32f
                )
                lineToRelative(32f, 32f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11.32f, 0f)
                lineToRelative(32f, -32f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.32f,
                    -11.32f
                )
                close()
            }
        }.build()

        return _CloudArrowDown!!
    }

@Suppress("ObjectPropertyName")
private var _CloudArrowDown: ImageVector? = null
