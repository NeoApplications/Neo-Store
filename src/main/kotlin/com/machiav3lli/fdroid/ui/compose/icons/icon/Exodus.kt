package com.machiav3lli.fdroid.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Icon

val Icon.Exodus: ImageVector
    get() {
        if (_ExodusSimple != null) {
            return _ExodusSimple!!
        }
        _ExodusSimple = ImageVector.Builder(
            name = "Exodus",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 612f,
            viewportHeight = 612f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                strokeLineWidth = 0.589891f
            ) {
                moveTo(254.52f, 296.37f)
                curveTo(202.34f, 279.39f, 176.24f, 253.7f, 176.24f, 219.27f)
                curveToRelative(-0f, -27.15f, 13.84f, -48.85f, 41.52f, -65.09f)
                curveToRelative(27.68f, -16.24f, 62.05f, -24.36f, 103.11f, -24.37f)
                curveToRelative(37.35f, 0f, 67.02f, 5.64f, 89.01f, 16.91f)
                curveToRelative(21.99f, 11.27f, 32.98f, 24.79f, 32.98f, 40.55f)
                curveToRelative(-0f, 8.24f, -3.44f, 15.46f, -10.33f, 21.64f)
                curveToRelative(-6.89f, 6.18f, -14.83f, 9.27f, -23.84f, 9.27f)
                curveToRelative(-14.83f, 0f, -27.02f, -9.45f, -36.56f, -28.37f)
                curveToRelative(-13.25f, -26.18f, -33.11f, -39.27f, -59.6f, -39.28f)
                curveToRelative(-20.93f, 0f, -45.09f, 6.3f, -58.6f, 18.91f)
                curveToRelative(-13.51f, 12.61f, -25.8f, 22.31f, -25.8f, 44.86f)
                curveToRelative(-0f, 44.37f, 26.63f, 63.66f, 76.7f, 63.66f)
                curveToRelative(0f, 0f, 23.53f, -7.11f, 30.41f, -8.08f)
                curveToRelative(11.92f, -1.45f, 22.9f, -0.59f, 29.52f, -0.59f)
                curveToRelative(18.82f, 5.55f, 21.27f, 20.05f, 21.27f, 28.53f)
                curveToRelative(-0f, 9.46f, -8.21f, 14.18f, -24.64f, 14.18f)
                curveToRelative(-5.83f, 0f, -14.57f, -0.85f, -26.23f, -2.55f)
                curveToRelative(-8.74f, -1.45f, -15.5f, -2.18f, -20.26f, -2.18f)
                curveToRelative(-52.98f, 0f, -79.47f, 24.73f, -79.47f, 74.19f)
                curveToRelative(-0f, 24f, -0.63f, 38.3f, 21.06f, 58f)
                curveToRelative(21.38f, 11.78f, 34.83f, 11.48f, 60f, 11.48f)
                curveToRelative(31.52f, 0f, 51.26f, -4.38f, 61.59f, -34.2f)
                curveToRelative(5.3f, -15.76f, 11.19f, -26.67f, 17.68f, -32.73f)
                curveToRelative(6.49f, -6.06f, 15.17f, -9.09f, 26.03f, -9.09f)
                curveToRelative(9.01f, 0f, 17.15f, 2.97f, 24.44f, 8.91f)
                curveToRelative(7.28f, 5.94f, 10.93f, 13.52f, 10.93f, 22.73f)
                curveToRelative(-0f, 22.06f, -13.51f, 40.31f, -40.53f, 54.73f)
                curveToRelative(-27.02f, 14.43f, -59.74f, 21.64f, -98.15f, 21.64f)
                curveToRelative(-42.12f, 0f, -79.54f, -9.21f, -112.25f, -27.64f)
                curveToRelative(-32.72f, -18.43f, -49.07f, -43.03f, -49.07f, -73.82f)
                curveToRelative(-0f, -38.55f, 32.45f, -66.91f, 97.35f, -85.1f)
                close()
            }
        }.build()

        return _ExodusSimple!!
    }

@Suppress("ObjectPropertyName")
private var _ExodusSimple: ImageVector? = null
