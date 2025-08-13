package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.WifiHigh: ImageVector
    get() {
        if (_WifiHigh != null) {
            return _WifiHigh!!
        }
        _WifiHigh = ImageVector.Builder(
            name = "WifiHigh",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(140f, 204f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 140f, 204f)
                close()
                moveTo(237.08f, 87f)
                arcTo(172f, 172f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.92f, 87f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 29.08f, 99.37f)
                arcToRelative(
                    156f,
                    156f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    197.84f,
                    0f
                )
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 237.08f, 87f)
                close()
                moveTo(205f, 122.77f)
                arcToRelative(
                    124f,
                    124f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -153.94f,
                    0f
                )
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 61f, 135.31f)
                arcToRelative(
                    108f,
                    108f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    134.06f,
                    0f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    11.24f,
                    -1.3f
                )
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 205f, 122.77f)
                close()
                moveTo(172.74f, 158.53f)
                arcToRelative(
                    76.05f,
                    76.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -89.42f,
                    0f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    9.42f,
                    12.94f
                )
                arcToRelative(
                    60f,
                    60f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    70.58f,
                    0f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    9.42f,
                    -12.94f
                )
                close()
            }
        }.build()

        return _WifiHigh!!
    }

@Suppress("ObjectPropertyName")
private var _WifiHigh: ImageVector? = null
