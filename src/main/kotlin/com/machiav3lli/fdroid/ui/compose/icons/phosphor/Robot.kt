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

val Phosphor.Robot: ImageVector
    get() {
        if (_robot != null) {
            return _robot!!
        }
        _robot = Builder(
            name = "Robot",
            defaultWidth = 32.0.dp,
            defaultHeight = 32.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(200.0f, 48.0f)
                lineTo(136.0f, 48.0f)
                lineTo(136.0f, 16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                lineTo(120.0f, 48.0f)
                lineTo(56.0f, 48.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 24.0f, 80.0f)
                lineTo(24.0f, 192.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, 32.0f)
                lineTo(200.0f, 224.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, -32.0f)
                lineTo(232.0f, 80.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 200.0f, 48.0f)
                close()
                moveTo(216.0f, 192.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(56.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(40.0f, 80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 56.0f, 64.0f)
                lineTo(200.0f, 64.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                close()
                moveTo(164.0f, 136.0f)
                lineTo(92.0f, 136.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, false, false, 0.0f, 56.0f)
                horizontalLineToRelative(72.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, false, false, 0.0f, -56.0f)
                close()
                moveTo(140.0f, 152.0f)
                verticalLineToRelative(24.0f)
                lineTo(116.0f, 176.0f)
                lineTo(116.0f, 152.0f)
                close()
                moveTo(80.0f, 164.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, 12.0f, -12.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(24.0f)
                lineTo(92.0f, 176.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 80.0f, 164.0f)
                close()
                moveTo(164.0f, 176.0f)
                horizontalLineToRelative(-8.0f)
                lineTo(156.0f, 152.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, 0.0f, 24.0f)
                close()
                moveTo(72.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 72.0f, 108.0f)
                close()
                moveTo(160.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 160.0f, 108.0f)
                close()
            }
        }
            .build()
        return _robot!!
    }

private var _robot: ImageVector? = null
