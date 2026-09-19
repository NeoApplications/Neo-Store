import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor

val Phosphor.Sword: ImageVector
    get() {
        if (_Sword != null) {
            return _Sword!!
        }
        _Sword = ImageVector.Builder(
            name = "Sword",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(216f, 32f)
                lineTo(152f, 32f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -6.34f,
                    3.12f
                )
                lineToRelative(-64f, 83.21f)
                lineTo(72f, 108.69f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -22.64f,
                    0f
                )
                lineToRelative(-12.69f, 12.7f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    22.63f
                )
                lineToRelative(20f, 20f)
                lineToRelative(-28f, 28f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    22.63f
                )
                lineToRelative(12.69f, 12.68f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    22.62f,
                    0f
                )
                lineToRelative(28f, -28f)
                lineToRelative(20f, 20f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    22.64f,
                    0f
                )
                lineToRelative(12.69f, -12.7f)
                arcToRelative(
                    16f,
                    16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -22.63f
                )
                lineToRelative(-9.64f, -9.64f)
                lineToRelative(83.21f, -64f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 224f, 104f)
                lineTo(224f, 40f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 216f, 32f)
                close()
                moveTo(52.69f, 216f)
                lineTo(40f, 203.32f)
                lineToRelative(28f, -28f)
                lineTo(80.68f, 188f)
                close()
                moveTo(123.3f, 208f)
                lineTo(48f, 132.71f)
                lineTo(60.7f, 120f)
                lineTo(136f, 195.31f)
                close()
                moveTo(208f, 100.06f)
                lineToRelative(-81.74f, 62.88f)
                lineTo(115.32f, 152f)
                lineToRelative(50.34f, -50.34f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -11.32f,
                    -11.31f
                )
                lineTo(104f, 140.68f)
                lineTo(93.07f, 129.74f)
                lineTo(155.94f, 48f)
                lineTo(208f, 48f)
                close()
            }
        }.build()

        return _Sword!!
    }

@Suppress("ObjectPropertyName")
private var _Sword: ImageVector? = null
