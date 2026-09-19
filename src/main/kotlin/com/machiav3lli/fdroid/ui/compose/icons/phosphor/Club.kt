package com.machiav3lli.fdroid.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Club: ImageVector
    get() {
        if (_Club != null) {
            return _Club!!
        }
        _Club = ImageVector.Builder(
            name = "Club",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(184f, 88f)
                curveToRelative(-0.78f, 0f, -1.56f, 0f, -2.33f, 0f)
                arcToRelative(
                    56f,
                    56f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -107.34f,
                    0f
                )
                curveToRelative(-0.78f, 0f, -1.55f, 0f, -2.33f, 0f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = false, 96.54f, 194.35f)
                lineToRelative(-8.2f, 27.35f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, 232f)
                horizontalLineToRelative(64f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    7.66f,
                    -10.3f
                )
                lineToRelative(-8.2f, -27.35f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = false, 184f, 88f)
                close()
                moveTo(184f, 184f)
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -33.4f,
                    -18f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -14.33f,
                    6.71f
                )
                lineToRelative(13f, 43.26f)
                horizontalLineToRelative(-42.5f)
                lineToRelative(13f, -43.26f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 105.4f, 166f)
                arcToRelative(
                    40f,
                    40f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    -19.93f,
                    -59.71f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    9.33f,
                    -12f
                )
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = true, isPositiveArc = true, 66.4f, 0f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.33f, 12f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = true, isPositiveArc = true, 184f, 184f)
                close()
            }
        }.build()

        return _Club!!
    }

@Suppress("ObjectPropertyName")
private var _Club: ImageVector? = null
