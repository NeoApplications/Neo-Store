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

val Phosphor.Textbox: ImageVector
    get() {
        if (_textbox != null) {
            return _textbox!!
        }
        _textbox = Builder(
            name = "Textbox",
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
                moveTo(112.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineTo(64.0f)
                horizontalLineTo(24.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 8.0f, 80.0f)
                verticalLineToRelative(96.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineToRelative(80.0f)
                verticalLineToRelative(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(48.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 112.0f, 40.0f)
                close()
                moveTo(24.0f, 176.0f)
                verticalLineTo(80.0f)
                horizontalLineToRelative(80.0f)
                verticalLineToRelative(96.0f)
                close()
                moveTo(248.0f, 80.0f)
                verticalLineToRelative(96.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                horizontalLineTo(144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(88.0f)
                verticalLineTo(80.0f)
                horizontalLineTo(144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(88.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 248.0f, 80.0f)
                close()
                moveTo(86.0f, 112.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(72.0f)
                verticalLineToRelative(28.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                verticalLineTo(120.0f)
                horizontalLineTo(50.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineTo(78.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 86.0f, 112.0f)
                close()
            }
        }
            .build()
        return _textbox!!
    }

private var _textbox: ImageVector? = null


@Preview
@Composable
fun TextboxPreview() {
    Image(
        Phosphor.Textbox,
        null
    )
}
