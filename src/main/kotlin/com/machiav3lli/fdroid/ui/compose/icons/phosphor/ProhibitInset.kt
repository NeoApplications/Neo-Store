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

val Phosphor.ProhibitInset: ImageVector
    get() {
        if (_prohibit_inset != null) {
            return _prohibit_inset!!
        }
        _prohibit_inset = Builder(
            name = "Prohibit-inset",
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
                moveTo(128.0f, 24.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, false, 232.0f, 128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(128.0f, 216.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 88.0f, -88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 128.0f, 216.0f)
                close()
                moveTo(161.9f, 150.6f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, 11.3f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -5.6f, 2.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.7f, -2.4f)
                lineTo(94.1f, 105.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, -11.3f)
                close()
            }
        }
            .build()
        return _prohibit_inset!!
    }

private var _prohibit_inset: ImageVector? = null


@Preview
@Composable
fun ProhibitInsetPreview() {
    Image(
        Phosphor.ProhibitInset,
        null
    )
}
