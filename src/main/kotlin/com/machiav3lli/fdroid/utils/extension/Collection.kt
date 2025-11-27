package com.machiav3lli.fdroid.utils.extension

import java.text.Collator

private val collator = Collator.getInstance().apply {
    strength = Collator.PRIMARY
}

fun <T> Collection<T>.sortedLocalized(selector: T.() -> String): List<T> =
    sortedWith { a, b -> collator.compare(a.selector(), b.selector()) }

fun <T> Collection<T>.sortedLocalizedDescending(selector: T.() -> String): List<T> =
    sortedWith { a, b -> collator.compare(b.selector(), a.selector()) }