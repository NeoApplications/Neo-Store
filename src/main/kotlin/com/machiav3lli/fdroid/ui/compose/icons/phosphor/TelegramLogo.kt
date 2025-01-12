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

val Phosphor.TelegramLogo: ImageVector
    get() {
        if (_telegram_logo != null) {
            return _telegram_logo!!
        }
        _telegram_logo = Builder(
            name = "Telegram-logo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(231.3f, 31.7f)
                arcTo(16.1f, 16.1f, 0.0f, false, false, 215.0f, 29.0f)
                lineTo(30.4f, 101.5f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, false, -10.1f, 16.3f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 12.8f, 14.3f)
                lineTo(80.0f, 141.4f)
                lineTo(80.0f, 200.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 9.9f, 14.8f)
                arcTo(16.6f, 16.6f, 0.0f, false, false, 96.0f, 216.0f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, false, 11.3f, -4.7f)
                lineToRelative(26.0f, -25.9f)
                lineTo(172.6f, 220.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 10.5f, 4.0f)
                arcToRelative(14.2f, 14.2f, 0.0f, false, false, 5.0f, -0.8f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 10.7f, -11.6f)
                lineTo(236.4f, 47.4f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 231.3f, 31.7f)
                close()
                moveTo(86.1f, 126.3f)
                lineToRelative(-49.8f, -9.9f)
                lineTo(175.9f, 61.5f)
                close()
                moveTo(96.0f, 200.0f)
                lineTo(96.0f, 152.6f)
                lineToRelative(25.2f, 22.2f)
                close()
                moveTo(183.2f, 208.0f)
                lineTo(100.8f, 135.5f)
                lineTo(219.5f, 49.8f)
                close()
            }
        }
            .build()
        return _telegram_logo!!
    }

private var _telegram_logo: ImageVector? = null
