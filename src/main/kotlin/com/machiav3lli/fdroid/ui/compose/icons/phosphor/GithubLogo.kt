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

val Phosphor.GithubLogo: ImageVector
    get() {
        if (_github_logo != null) {
            return _github_logo!!
        }
        _github_logo = Builder(
            name = "Github-logo",
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
                moveTo(216.0f, 216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineToRelative(-8.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, -14.8f, -27.0f)
                arcTo(55.8f, 55.8f, 0.0f, false, false, 208.0f, 120.0f)
                verticalLineToRelative(-8.0f)
                arcToRelative(58.0f, 58.0f, 0.0f, false, false, -7.7f, -28.3f)
                arcTo(59.9f, 59.9f, 0.0f, false, false, 194.9f, 36.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, -6.9f, -4.0f)
                arcToRelative(59.7f, 59.7f, 0.0f, false, false, -48.0f, 24.0f)
                horizontalLineTo(116.0f)
                arcTo(59.7f, 59.7f, 0.0f, false, false, 68.0f, 32.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, -6.9f, 4.0f)
                arcToRelative(59.9f, 59.9f, 0.0f, false, false, -5.4f, 47.7f)
                arcTo(58.0f, 58.0f, 0.0f, false, false, 48.0f, 112.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(55.8f, 55.8f, 0.0f, false, false, 22.8f, 45.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 56.0f, 192.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, -32.0f)
                verticalLineToRelative(-8.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, -32.0f)
                verticalLineTo(176.0f)
                horizontalLineToRelative(24.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineTo(176.0f)
                horizontalLineToRelative(12.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(64.0f, 120.0f)
                verticalLineToRelative(-8.0f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 6.9f, -22.5f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 72.0f, 81.8f)
                arcToRelative(43.7f, 43.7f, 0.0f, false, true, 0.8f, -33.5f)
                arcToRelative(43.6f, 43.6f, 0.0f, false, true, 32.3f, 20.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.7f, 3.7f)
                horizontalLineToRelative(32.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.7f, -3.7f)
                arcToRelative(43.6f, 43.6f, 0.0f, false, true, 32.3f, -20.0f)
                arcToRelative(43.7f, 43.7f, 0.0f, false, true, 0.8f, 33.5f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 1.1f, 7.7f)
                arcTo(42.7f, 42.7f, 0.0f, false, true, 192.0f, 112.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, -40.0f, 40.0f)
                horizontalLineTo(104.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 64.0f, 120.0f)
                close()
            }
        }
            .build()
        return _github_logo!!
    }

private var _github_logo: ImageVector? = null
