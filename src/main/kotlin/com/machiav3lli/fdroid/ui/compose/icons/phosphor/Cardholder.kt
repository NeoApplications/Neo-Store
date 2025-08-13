package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Cardholder: ImageVector
    get() {
        if (_Cardholder != null) {
            return _Cardholder!!
        }
        _Cardholder = ImageVector.Builder(
            name = "Cardholder",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(208f, 48f)
                lineTo(48f, 48f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 72f)
                lineTo(24f, 184f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 24f)
                lineTo(208f, 208f)
                arcToRelative(
                    24f,
                    24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    24f,
                    -24f
                )
                lineTo(232f, 72f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 208f, 48f)
                close()
                moveTo(40f, 96f)
                lineTo(216f, 96f)
                verticalLineToRelative(16f)
                lineTo(160f, 112f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, 8f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -48f, 0f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, -8f)
                lineTo(40f, 112f)
                close()
                moveTo(48f, 64f)
                lineTo(208f, 64f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 8f)
                verticalLineToRelative(8f)
                lineTo(40f, 80f)
                lineTo(40f, 72f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 64f)
                close()
                moveTo(208f, 192f)
                lineTo(48f, 192f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, -8f)
                lineTo(40f, 128f)
                lineTo(88.8f, 128f)
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    78.4f,
                    0f
                )
                lineTo(216f, 128f)
                verticalLineToRelative(56f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 208f, 192f)
                close()
            }
        }.build()

        return _Cardholder!!
    }

@Suppress("ObjectPropertyName")
private var _Cardholder: ImageVector? = null
