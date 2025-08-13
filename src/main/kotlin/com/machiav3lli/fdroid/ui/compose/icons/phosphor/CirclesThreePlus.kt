package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.CirclesThreePlus: ImageVector
    get() {
        if (_CirclesThreePlus != null) {
            return _CirclesThreePlus!!
        }
        _CirclesThreePlus = ImageVector.Builder(
            name = "CirclesThreePlus",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(80f, 40f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = true, isPositiveArc = false, 40f, 40f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 40f)
                close()
                moveTo(80f, 104f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, 24f, -24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 80f, 104f)
                close()
                moveTo(176f, 120f)
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -40f,
                    -40f
                )
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, 176f, 120f)
                close()
                moveTo(176f, 56f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 176f, 56f)
                close()
                moveTo(80f, 136f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = true, isPositiveArc = false, 40f, 40f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 136f)
                close()
                moveTo(80f, 200f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, 24f, -24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 80f, 200f)
                close()
                moveTo(216f, 176f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, 8f)
                lineTo(184f, 184f)
                verticalLineToRelative(24f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 0f)
                lineTo(168f, 184f)
                lineTo(144f, 184f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -16f)
                horizontalLineToRelative(24f)
                lineTo(168f, 144f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 0f)
                verticalLineToRelative(24f)
                horizontalLineToRelative(24f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 216f, 176f)
                close()
            }
        }.build()

        return _CirclesThreePlus!!
    }

@Suppress("ObjectPropertyName")
private var _CirclesThreePlus: ImageVector? = null
