package com.machiav3lli.fdroid.ui.compose.icons.phosphor

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

val Phosphor.CurrencyBTC: ImageVector
    get() {
        if (_currency_btc != null) {
            return _currency_btc!!
        }
        _currency_btc = Builder(
            name = "Currency-btc",
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
                moveTo(170.5f, 115.7f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 144.0f, 40.2f)
                verticalLineTo(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineTo(40.0f)
                horizontalLineTo(112.0f)
                verticalLineTo(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineTo(40.0f)
                horizontalLineTo(64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(8.0f)
                verticalLineTo(192.0f)
                horizontalLineTo(64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineTo(96.0f)
                verticalLineToRelative(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(208.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(208.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, 18.5f, -92.3f)
                close()
                moveTo(168.0f, 84.0f)
                arcToRelative(28.1f, 28.1f, 0.0f, false, true, -28.0f, 28.0f)
                horizontalLineTo(88.0f)
                verticalLineTo(56.0f)
                horizontalLineToRelative(52.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, true, 168.0f, 84.0f)
                close()
                moveTo(152.0f, 192.0f)
                horizontalLineTo(88.0f)
                verticalLineTo(128.0f)
                horizontalLineToRelative(64.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 0.0f, 64.0f)
                close()
            }
        }
            .build()
        return _currency_btc!!
    }

private var _currency_btc: ImageVector? = null
