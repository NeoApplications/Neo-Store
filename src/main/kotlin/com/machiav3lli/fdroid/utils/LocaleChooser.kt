package com.machiav3lli.fdroid.utils

import android.icu.util.ULocale
import android.util.LruCache
import androidx.core.os.LocaleListCompat
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.PREFS_LANGUAGE_DEFAULT
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import java.util.Locale

object LocaleChooser {
    var defaultLocale: Locale = Locale.getDefault()
    var defaultLocaleList: LocaleListCompat = LocaleListCompat.getDefault()

    fun setLanguage(locale: String) {
        if (locale == PREFS_LANGUAGE_DEFAULT && Locale.getDefault() != defaultLocale) {
            Locale.setDefault(defaultLocale)
        }
        NeoApp.refreshLanguage()
    }

    fun createLocaleConfig() = LocaleConfig()

    data class LocaleConfig(
        val localePref: String = Preferences[Preferences.Key.Language],
        val localeList: LocaleListCompat = defaultLocaleList,
    ) {
        val prefList: List<Locale>
        val prefScript: List<String?>
        val prefCountry: List<String?>
        private val cache by lazy { LruCache<String, Int>(1000) }

        init {
            val locale = if (localePref.isBlank() || localePref == PREFS_LANGUAGE_DEFAULT) {
                null
            } else {
                Locale.forLanguageTag(localePref.replace("-r", "-"))
            }
            val pos = if (locale == null) -2 else localeList.indexOf(locale)
            val fallback =
                if (locale == FALLBACK_LOCALE) -2 else localeList.indexOf(FALLBACK_LOCALE)
            val systemLocales = List(localeList.size()) { localeList.get(it)!! }

            prefList = buildList {
                if (locale != null) add(locale)
                systemLocales.forEachIndexed { idx, l -> if (idx != pos) add(l) }
                if (fallback == -1) add(FALLBACK_LOCALE)
            }
            prefScript = prefList.map { it.maximizedScript() }
            prefCountry = prefScript.mapIndexed { i, script ->
                script?.let {
                    ULocale.addLikelySubtags(
                        ULocale(
                            prefList[i].language,
                            it
                        )
                    ).country.nullIfEmpty()
                }
            }
        }

        fun getWeight(availableLocale: String): Int =
            cache.get(availableLocale)
                ?: computeWeight(availableLocale).also { cache.put(availableLocale, it) }

        private fun computeWeight(availableLocale: String): Int {
            val locale =
                Locale.forLanguageTag(availableLocale.replace('_', '-').replace("-r", "-"))
            val script = locale.maximizedScript()

            val idx = prefList.indices.firstOrNull {
                prefList[it].language == locale.language && script == prefScript[it]
            } ?: return -1

            val l = prefList[idx]
            val sameCountry = l.country == locale.country
            val tieBreak = if (sameCountry) {
                if (locale.script == l.script) 0 else 2
            } else {
                (if (locale.country == prefCountry[idx]) 0 else 1) or
                        (if (locale.script.isEmpty()) 0 else 2) or
                        (if (locale.country.isEmpty()) 0 else 4) or 8
            }
            return ((idx + 1) shl 4) or tieBreak
        }

        companion object {
            private val FALLBACK_LOCALE = Locale.ENGLISH
        }
    }
}

private fun Locale.maximizedScript(): String? =
    script.nullIfEmpty() ?: ULocale.addLikelySubtags(ULocale.forLocale(this)).script.nullIfEmpty()
