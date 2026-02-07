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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Hash: ImageVector
    get() {
        if (_hash != null) {
            return _hash!!
        }
        _hash = Builder(
            name = "Hash",
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
                moveTo(224.0f, 88.0f)
                lineTo(175.4f, 88.0f)
                lineToRelative(8.5f, -46.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -15.8f, -2.8f)
                lineToRelative(-9.0f, 49.4f)
                lineTo(111.4f, 88.0f)
                lineToRelative(8.5f, -46.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -15.8f, -2.8f)
                lineTo(95.1f, 88.0f)
                lineTo(43.6f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 0.0f, 16.0f)
                lineTo(92.2f, 104.0f)
                lineToRelative(-8.7f, 48.0f)
                lineTo(32.0f, 152.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(80.6f, 168.0f)
                lineToRelative(-8.5f, 46.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.5f, 9.3f)
                lineTo(80.0f, 223.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.9f, -6.6f)
                lineToRelative(9.0f, -49.4f)
                horizontalLineToRelative(47.7f)
                lineToRelative(-8.5f, 46.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.5f, 9.3f)
                lineTo(144.0f, 223.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.9f, -6.6f)
                lineToRelative(9.0f, -49.4f)
                horizontalLineToRelative(51.5f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(163.8f, 151.8f)
                lineToRelative(8.7f, -48.0f)
                lineTo(224.0f, 103.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(147.5f, 152.0f)
                lineTo(99.8f, 152.0f)
                lineToRelative(8.7f, -48.0f)
                horizontalLineToRelative(47.7f)
                close()
            }
        }
            .build()
        return _hash!!
    }

private var _hash: ImageVector? = null


@Preview
@Composable
fun HashPreview() {
    Image(
        Phosphor.Hash,
        null
    )
}
