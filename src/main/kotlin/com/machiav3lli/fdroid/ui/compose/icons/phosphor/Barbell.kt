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

val Phosphor.Barbell: ImageVector
    get() {
        if (_barbell != null) {
            return _barbell!!
        }
        _barbell = Builder(
            name = "Barbell",
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
                moveTo(248.0f, 120.0f)
                horizontalLineToRelative(-8.0f)
                lineTo(240.0f, 88.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(208.0f, 72.0f)
                lineTo(208.0f, 64.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(168.0f, 48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, 16.0f)
                verticalLineToRelative(56.0f)
                lineTo(104.0f, 120.0f)
                lineTo(104.0f, 64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 88.0f, 48.0f)
                lineTo(64.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 48.0f, 64.0f)
                verticalLineToRelative(8.0f)
                lineTo(32.0f, 72.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 16.0f, 88.0f)
                verticalLineToRelative(32.0f)
                lineTo(8.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(48.0f, 184.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(88.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(104.0f, 136.0f)
                horizontalLineToRelative(48.0f)
                verticalLineToRelative(56.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineToRelative(24.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(16.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(240.0f, 136.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(32.0f, 168.0f)
                lineTo(32.0f, 88.0f)
                lineTo(48.0f, 88.0f)
                verticalLineToRelative(80.0f)
                close()
                moveTo(88.0f, 192.0f)
                lineTo(64.0f, 192.0f)
                lineTo(64.0f, 64.0f)
                lineTo(88.0f, 64.0f)
                verticalLineToRelative(63.9f)
                horizontalLineToRelative(0.0f)
                lineTo(88.0f, 192.0f)
                close()
                moveTo(192.0f, 192.0f)
                lineTo(168.0f, 192.0f)
                lineTo(168.0f, 64.0f)
                horizontalLineToRelative(24.0f)
                lineTo(192.0f, 80.0f)
                horizontalLineToRelative(0.0f)
                lineTo(192.0f, 192.0f)
                close()
                moveTo(224.0f, 168.0f)
                lineTo(208.0f, 168.0f)
                lineTo(208.0f, 88.0f)
                horizontalLineToRelative(16.0f)
                close()
            }
        }
            .build()
        return _barbell!!
    }

private var _barbell: ImageVector? = null
