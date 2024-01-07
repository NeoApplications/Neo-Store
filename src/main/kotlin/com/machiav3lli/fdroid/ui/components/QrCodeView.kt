package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder

@Composable
fun QrCodeImage(
    modifier: Modifier = Modifier,
    content: String,
    contentDescription: String,
    foregroundColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    val qrEncoder = ZxingQrEncoder()
    val bitmapMatrix = remember(content) { qrEncoder.encode(content) }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor),
        contentDescription = contentDescription,
    ) {

        bitmapMatrix?.let { matrix ->
            val cellSize = size.width / matrix.width

            for (x in 0 until matrix.width) {
                for (y in 0 until matrix.height) {
                    if (matrix.get(x, y) != 1.toByte() || isFinder(x, y, matrix.width)) continue

                    drawRect(
                        color = foregroundColor,
                        topLeft = Offset(x * cellSize, y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
            drawFinderSquare(cellSize, Offset(0f, 0f), foregroundColor, backgroundColor)
            drawFinderSquare(
                cellSize,
                Offset(size.width - FINDER_SIZE * cellSize, 0f),
                foregroundColor,
                backgroundColor,
            )
            drawFinderSquare(
                cellSize,
                Offset(0f, size.width - FINDER_SIZE * cellSize),
                foregroundColor,
                backgroundColor,
            )
        }
    }
}

const val FINDER_SIZE = 7

internal fun isFinder(x: Int, y: Int, gridSize: Int, finderSize: Int = FINDER_SIZE) =
    (x < finderSize && y < finderSize) ||
            (x < finderSize && y > gridSize - 1 - finderSize) ||
            (x > gridSize - 1 - finderSize && y < finderSize)

private fun DrawScope.drawFinderSquare(
    cellSize: Float,
    topLeft: Offset,
    foregroundColor: Color,
    backgroundColor: Color,
    finderSize: Int = 7,
) {
    drawRect(
        color = foregroundColor,
        topLeft = topLeft,
        size = Size(cellSize * finderSize, cellSize * finderSize),
    )
    drawRect(
        color = backgroundColor,
        topLeft = topLeft + Offset(cellSize, cellSize),
        size = Size(
            cellSize * (finderSize - 2),
            cellSize * (finderSize - 2)
        ),
    )
    drawRect(
        color = foregroundColor,
        topLeft = topLeft + Offset(cellSize * 2, cellSize * 2),
        size = Size(
            cellSize * (finderSize - 4),
            cellSize * (finderSize - 4)
        ),
    )
}

class ZxingQrEncoder {
    fun encode(qrData: String): ByteMatrix? {
        return Encoder.encode(
            qrData,
            ErrorCorrectionLevel.H,
            mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 16,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            )
        ).matrix
    }
}