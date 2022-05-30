package com.saggitt.omega.compose.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun rememberExtendPadding(
    padding: PaddingValues,
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues = remember(start, top, end, bottom) {
    CombinePaddingValues(
        padding, PaddingValues(
            start = start,
            top = top,
            end = end,
            bottom = bottom
        )
    )
}

private class CombinePaddingValues(private val a: PaddingValues, private val b: PaddingValues) :
    PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
        a.calculateLeftPadding(layoutDirection) + b.calculateLeftPadding(layoutDirection)

    override fun calculateTopPadding() =
        a.calculateTopPadding() + b.calculateTopPadding()

    override fun calculateRightPadding(layoutDirection: LayoutDirection) =
        a.calculateRightPadding(layoutDirection) + b.calculateRightPadding(layoutDirection)

    override fun calculateBottomPadding() =
        a.calculateBottomPadding() + b.calculateBottomPadding()
}

@Composable
operator fun PaddingValues.minus(b: PaddingValues): PaddingValues {
    val a = this
    return remember(a, b) {
        object : PaddingValues {
            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
                val aLeft = a.calculateLeftPadding(layoutDirection)
                val bLeft = b.calculateRightPadding(layoutDirection)
                return (aLeft - bLeft).coerceAtLeast(0.dp)
            }

            override fun calculateTopPadding(): Dp {
                val aTop = a.calculateTopPadding()
                val bTop = b.calculateTopPadding()
                return (aTop - bTop).coerceAtLeast(0.dp)
            }

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
                val aRight = a.calculateRightPadding(layoutDirection)
                val bRight = b.calculateRightPadding(layoutDirection)
                return (aRight - bRight).coerceAtLeast(0.dp)
            }

            override fun calculateBottomPadding(): Dp {
                val aBottom = a.calculateBottomPadding()
                val bBottom = b.calculateBottomPadding()
                return (aBottom - bBottom).coerceAtLeast(0.dp)
            }
        }
    }
}
