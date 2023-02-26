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

val Phosphor.GameController: ImageVector
    get() {
        if (_game_controller != null) {
            return _game_controller!!
        }
        _game_controller = Builder(
            name = "Game-controller",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(184.0f, 116.0f)
                lineTo(152.0f, 116.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                close()
                moveTo(104.0f, 100.0f)
                lineTo(96.0f, 100.0f)
                lineTo(96.0f, 92.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                lineTo(72.0f, 100.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(218.3f, 223.4f)
                arcToRelative(33.6f, 33.6f, 0.0f, false, true, -6.3f, 0.6f)
                arcToRelative(36.0f, 36.0f, 0.0f, false, true, -25.4f, -10.5f)
                lineToRelative(-0.5f, -0.5f)
                lineToRelative(-40.6f, -45.2f)
                lineToRelative(-34.9f, 0.2f)
                lineTo(69.9f, 213.0f)
                lineToRelative(-0.4f, 0.5f)
                arcTo(36.4f, 36.4f, 0.0f, false, true, 44.0f, 224.0f)
                arcToRelative(31.8f, 31.8f, 0.0f, false, true, -6.2f, -0.6f)
                arcTo(35.9f, 35.9f, 0.0f, false, true, 8.6f, 181.7f)
                horizontalLineToRelative(0.0f)
                lineTo(24.9f, 97.8f)
                verticalLineToRelative(-0.2f)
                arcTo(59.9f, 59.9f, 0.0f, false, true, 84.0f, 48.0f)
                lineToRelative(88.0f, -0.3f)
                horizontalLineToRelative(0.0f)
                arcTo(60.0f, 60.0f, 0.0f, false, true, 231.0f, 97.0f)
                curveToRelative(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.2f)
                lineToRelative(16.3f, 84.4f)
                horizontalLineToRelative(0.1f)
                arcTo(36.1f, 36.1f, 0.0f, false, true, 218.3f, 223.4f)
                close()
                moveTo(172.0f, 151.7f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, false, 0.0f, -88.0f)
                lineTo(84.0f, 64.0f)
                arcToRelative(43.9f, 43.9f, 0.0f, false, false, -43.3f, 36.0f)
                arcToRelative(0.9f, 0.9f, 0.0f, false, true, -0.1f, 0.5f)
                lineTo(24.3f, 184.6f)
                arcTo(20.0f, 20.0f, 0.0f, false, false, 58.0f, 202.3f)
                lineToRelative(43.1f, -47.7f)
                arcTo(8.3f, 8.3f, 0.0f, false, true, 107.0f, 152.0f)
                close()
                moveTo(231.7f, 184.6f)
                lineTo(223.0f, 139.4f)
                arcToRelative(60.0f, 60.0f, 0.0f, false, true, -51.0f, 28.3f)
                horizontalLineToRelative(-5.1f)
                lineTo(198.0f, 202.3f)
                arcToRelative(20.0f, 20.0f, 0.0f, false, false, 33.7f, -17.7f)
                close()
            }
        }
            .build()
        return _game_controller!!
    }

private var _game_controller: ImageVector? = null
