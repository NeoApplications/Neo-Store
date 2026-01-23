package com.machiav3lli.fdroid.utils.extension

import com.machiav3lli.fdroid.data.entity.UpdateListItem
import java.text.Collator

private val collator = Collator.getInstance().apply {
    strength = Collator.PRIMARY
}

fun <T> Collection<T>.sortedLocalized(selector: T.() -> String): List<T> =
    sortedWith { a, b -> collator.compare(a.selector(), b.selector()) }

fun <T> Collection<T>.sortedLocalizedDescending(selector: T.() -> String): List<T> =
    sortedWith { a, b -> collator.compare(b.selector(), a.selector()) }

fun Collection<UpdateListItem>.partitionTypes(): Pair<List<UpdateListItem.UpdateItem>, List<UpdateListItem.DownloadOnlyItem>> =
    partition { it is UpdateListItem.UpdateItem } as Pair<List<UpdateListItem.UpdateItem>, List<UpdateListItem.DownloadOnlyItem>>