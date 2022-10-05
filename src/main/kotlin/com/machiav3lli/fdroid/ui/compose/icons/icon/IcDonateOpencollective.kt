package com.machiav3lli.fdroid.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Icon

val Icon.IcDonateOpencollective: ImageVector
    get() {
        if (_icDonateOpencollective != null) {
            return _icDonateOpencollective!!
        }
        _icDonateOpencollective = Builder(
            name = "IcDonateOpencollective",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(12.0f, 2.0f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 2.0f, 12.0f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 12.0f, 22.0f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 18.043f, 19.951f)
                lineTo(15.469f, 17.377f)
                arcTo(6.4f, 6.4f, 0.0f, false, true, 12.0f, 18.4f)
                arcTo(6.4f, 6.4f, 0.0f, false, true, 5.6f, 12.0f)
                arcTo(6.4f, 6.4f, 0.0f, false, true, 12.0f, 5.6f)
                arcTo(6.4f, 6.4f, 0.0f, false, true, 15.465f, 6.627f)
                lineTo(18.047f, 4.045f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 12.0f, 2.0f)
                close()
                moveTo(19.951f, 5.957f)
                lineTo(17.377f, 8.531f)
                arcTo(6.4f, 6.4f, 0.0f, false, true, 18.4f, 12.0f)
                arcTo(6.4f, 6.4f, 0.0f, false, true, 17.373f, 15.465f)
                lineTo(19.955f, 18.047f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 22.0f, 12.0f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 19.951f, 5.957f)
                close()
            }
        }
            .build()
        return _icDonateOpencollective!!
    }

private var _icDonateOpencollective: ImageVector? = null
