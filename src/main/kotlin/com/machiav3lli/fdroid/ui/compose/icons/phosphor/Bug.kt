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

val Phosphor.Bug: ImageVector
    get() {
        if (_bug != null) {
            return _bug!!
        }
        _bug = Builder(
            name = "Bug",
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
                moveTo(168.0f, 92.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 168.0f, 92.0f)
                close()
                moveTo(100.0f, 80.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 100.0f, 80.0f)
                close()
                moveTo(252.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(216.0f, 136.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(88.1f, 88.1f, 0.0f, false, true, -3.2f, 23.7f)
                lineToRelative(23.1f, 13.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -4.0f, 14.9f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -4.0f, -1.1f)
                lineTo(207.0f, 182.8f)
                arcToRelative(88.0f, 88.0f, 0.0f, false, true, -158.0f, 0.0f)
                lineTo(28.1f, 194.9f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -4.0f, 1.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -4.0f, -14.9f)
                lineToRelative(23.1f, -13.4f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 40.0f, 144.0f)
                verticalLineToRelative(-8.0f)
                lineTo(12.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                lineTo(40.0f, 120.0f)
                verticalLineToRelative(-8.0f)
                arcToRelative(88.1f, 88.1f, 0.0f, false, true, 3.2f, -23.7f)
                lineTo(20.1f, 74.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -3.0f, -10.9f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.0f, -2.9f)
                lineTo(49.0f, 73.2f)
                arcToRelative(88.0f, 88.0f, 0.0f, false, true, 158.0f, 0.0f)
                lineToRelative(20.9f, -12.1f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.0f, 2.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -3.0f, 10.9f)
                lineTo(212.8f, 88.3f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 216.0f, 112.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(28.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 252.0f, 128.0f)
                close()
                moveTo(120.0f, 136.0f)
                lineTo(56.0f, 136.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, false, false, 64.0f, 71.5f)
                close()
                moveTo(200.0f, 136.0f)
                lineTo(136.0f, 136.0f)
                verticalLineToRelative(79.5f)
                arcTo(72.0f, 72.0f, 0.0f, false, false, 200.0f, 144.0f)
                close()
                moveTo(200.0f, 112.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, false, false, -144.0f, 0.0f)
                verticalLineToRelative(8.0f)
                lineTo(200.0f, 120.0f)
                close()
            }
        }
            .build()
        return _bug!!
    }

private var _bug: ImageVector? = null
