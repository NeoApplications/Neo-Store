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

val Phosphor.Brain: ImageVector
    get() {
        if (_brain != null) {
            return _brain!!
        }
        _brain = Builder(
            name = "Brain",
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
                moveTo(248.0f, 132.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, false, -32.0f, -50.6f)
                lineTo(216.0f, 72.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, -88.0f, -26.5f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 40.0f, 72.0f)
                verticalLineToRelative(9.4f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, false, 0.0f, 101.2f)
                lineTo(40.0f, 184.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, 88.0f, 26.5f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 216.0f, 184.0f)
                verticalLineToRelative(-1.4f)
                arcTo(56.1f, 56.1f, 0.0f, false, false, 248.0f, 132.0f)
                close()
                moveTo(88.0f, 216.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -31.8f, -28.6f)
                arcToRelative(49.3f, 49.3f, 0.0f, false, false, 7.8f, 0.6f)
                horizontalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(64.0f, 172.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 50.7f, 94.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 56.0f, 86.7f)
                lineTo(56.0f, 72.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 64.0f, 0.0f)
                verticalLineToRelative(76.3f)
                arcTo(47.4f, 47.4f, 0.0f, false, false, 88.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 0.0f, 64.0f)
                close()
                moveTo(192.0f, 172.0f)
                horizontalLineToRelative(-8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(49.3f, 49.3f, 0.0f, false, false, 7.8f, -0.6f)
                arcTo(32.0f, 32.0f, 0.0f, true, true, 168.0f, 152.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                arcToRelative(47.4f, 47.4f, 0.0f, false, false, -32.0f, 12.3f)
                lineTo(136.0f, 72.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 64.0f, 0.0f)
                lineTo(200.0f, 86.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 5.3f, 7.6f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 192.0f, 172.0f)
                close()
                moveTo(60.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, false, 80.0f, 92.0f)
                lineTo(80.0f, 84.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                arcTo(36.0f, 36.0f, 0.0f, false, true, 60.0f, 128.0f)
                close()
                moveTo(204.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, false, true, -36.0f, -36.0f)
                lineTo(160.0f, 84.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(20.1f, 20.1f, 0.0f, false, false, 20.0f, 20.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 204.0f, 120.0f)
                close()
            }
        }
            .build()
        return _brain!!
    }

private var _brain: ImageVector? = null
