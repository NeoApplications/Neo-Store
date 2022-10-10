package com.saggitt.omega.compose.components

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return layoutInfo.visibleItemsInfo.getOrNull(absoluteIndex - layoutInfo.visibleItemsInfo.first().index)
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to)
        return

    val element = this.removeAt(from) ?: return
    this.add(to, element)
}