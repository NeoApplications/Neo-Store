package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Envelope: ImageVector
    get() {
        if (_Envelope != null) {
            return _Envelope!!
        }
        _Envelope = ImageVector.Builder(
            name = "Envelope",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(224f, 48f)
                lineTo(32f, 48f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, 8f)
                lineTo(24f, 192f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(216f, 208f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    16f,
                    -16f
                )
                lineTo(232f, 56f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 224f, 48f)
                close()
                moveTo(128f, 133.15f)
                lineTo(52.57f, 64f)
                lineTo(203.43f, 64f)
                close()
                moveTo(98.71f, 128f)
                lineTo(40f, 181.81f)
                lineTo(40f, 74.19f)
                close()
                moveTo(110.55f, 138.85f)
                lineTo(122.55f, 149.9f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10.82f, 0f)
                lineToRelative(12f, -11.05f)
                lineToRelative(58f, 53.15f)
                lineTo(52.57f, 192f)
                close()
                moveTo(157.29f, 128f)
                lineTo(216f, 74.18f)
                lineTo(216f, 181.82f)
                close()
            }
        }.build()

        return _Envelope!!
    }

@Suppress("ObjectPropertyName")
private var _Envelope: ImageVector? = null
