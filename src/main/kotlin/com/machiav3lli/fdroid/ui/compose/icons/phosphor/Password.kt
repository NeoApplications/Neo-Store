package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Password: ImageVector
    get() {
        if (_Password != null) {
            return _Password!!
        }
        _Password = ImageVector.Builder(
            name = "Password",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(48f, 56f)
                lineTo(48f, 200f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 0f)
                lineTo(32f, 56f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 0f)
                close()
                moveTo(140f, 110.5f)
                lineTo(120f, 117f)
                lineTo(120f, 96f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 0f)
                verticalLineToRelative(21f)
                lineTo(84f, 110.5f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -5f,
                    15.22f
                )
                lineToRelative(20f, 6.49f)
                lineToRelative(-12.34f, 17f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    12.94f,
                    9.4f
                )
                lineToRelative(12.34f, -17f)
                lineToRelative(12.34f, 17f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    12.94f,
                    -9.4f
                )
                lineToRelative(-12.34f, -17f)
                lineToRelative(20f, -6.49f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 140f, 110.5f)
                close()
                moveTo(246f, 115.64f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 236f, 110.5f)
                lineTo(216f, 117f)
                lineTo(216f, 96f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 0f)
                verticalLineToRelative(21f)
                lineToRelative(-20f, -6.49f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -4.95f,
                    15.22f
                )
                lineToRelative(20f, 6.49f)
                lineToRelative(-12.34f, 17f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    12.94f,
                    9.4f
                )
                lineToRelative(12.34f, -17f)
                lineToRelative(12.34f, 17f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    12.94f,
                    -9.4f
                )
                lineToRelative(-12.34f, -17f)
                lineToRelative(20f, -6.49f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 246f, 115.64f)
                close()
            }
        }.build()

        return _Password!!
    }

@Suppress("ObjectPropertyName")
private var _Password: ImageVector? = null
