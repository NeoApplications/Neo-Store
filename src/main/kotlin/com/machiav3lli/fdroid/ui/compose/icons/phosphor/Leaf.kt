package com.machiav3lli.fdroid.ui.compose.icons.phosphor


import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Leaf: ImageVector
    get() {
        if (_leaf != null) {
            return _leaf!!
        }
        _leaf = Builder(
            name = "Leaf",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(224.0f, 39.5f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -7.5f, -7.5f)
                curveTo(140.2f, 27.5f, 79.1f, 50.5f, 53.0f, 93.6f)
                arcToRelative(87.3f, 87.3f, 0.0f, false, false, -12.8f, 49.1f)
                curveToRelative(0.6f, 15.9f, 5.2f, 32.1f, 13.8f, 48.0f)
                lineTo(34.3f, 210.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineTo(65.3f, 202.0f)
                curveToRelative(15.9f, 8.6f, 32.1f, 13.2f, 48.0f, 13.8f)
                horizontalLineToRelative(3.3f)
                arcTo(87.0f, 87.0f, 0.0f, false, false, 162.4f, 203.0f)
                curveTo(205.5f, 176.9f, 228.5f, 115.8f, 224.0f, 39.5f)
                close()
                moveTo(154.2f, 189.3f)
                curveToRelative(-22.9f, 13.8f, -49.9f, 14.0f, -77.0f, 0.8f)
                lineToRelative(88.5f, -88.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, -11.4f)
                lineTo(65.9f, 178.8f)
                curveToRelative(-13.2f, -27.1f, -13.0f, -54.1f, 0.8f, -77.0f)
                curveToRelative(22.1f, -36.5f, 74.8f, -56.5f, 141.7f, -54.2f)
                curveTo(210.7f, 114.5f, 190.7f, 167.2f, 154.2f, 189.3f)
                close()
            }
        }
            .build()
        return _leaf!!
    }

private var _leaf: ImageVector? = null


//@Preview
@Composable
fun LeafPreview() {
    Image(
        Phosphor.Leaf,
        null
    )
}
