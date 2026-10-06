package com.machiav3lli.fdroid.utils

import android.icu.util.ULocale;
import android.util.LruCache
import androidx.core.os.LocaleListCompat
import androidx.core.text.ICUCompat
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
        val localeList: LocaleListCompat = LocaleChooser.defaultLocaleList,
    ) {
        val prefList: List<Locale>
        val prefScript: List<String?>
        val prefCountry: List<String?>
        val cache by lazy { LruCache<String, Int>(1000) }

        init {
            val locale = if (localePref.isBlank() || localePref == PREFS_LANGUAGE_DEFAULT) {
                null
            } else {
                Locale.forLanguageTag(localePref.replaceFirst("-r", "-"))
            }
            val pos = if (locale == null) -2 else localeList.indexOf(locale)
            val fallback = if (locale == FALLBACK_LOCALE) -2 else localeList.indexOf(FALLBACK_LOCALE)
            val lsize = localeList.size()
            val size = lsize + (if (pos == -1) 1 else 0) + (if (fallback == -1) 1 else 0)
            var offset = if (pos == 0) 1 else 0
            prefList = List(size) {
                if (it == 0 && pos != -2) {
                    locale!!
                } else {
                    if (offset == pos) offset++
                    if (offset < lsize) {
                        localeList.get(offset++)!!
                    } else {
                        FALLBACK_LOCALE
                    }
                }
            }
            prefScript = List(size) {
                val l = prefList[it]
                l.script.nullIfEmpty() ?: ICUCompat.maximizeAndGetScript(l)?.nullIfEmpty()
            }
            prefCountry = List(size) {
                val s = prefScript[it]
                if (s == null) {
                    null
                } else {
                    val l = ULocale(prefList[it].language, s)
                    ULocale.addLikelySubtags(l).country.nullIfEmpty()
                }
            }
        }

        fun getWeight(available_locale: String): Int {
            return cache.get(available_locale) ?: run {
                val locale = Locale.forLanguageTag(available_locale.replace('_', '-').replaceFirst("-r", "-"))
                val script = locale.script.nullIfEmpty() ?: ICUCompat.maximizeAndGetScript(locale)?.nullIfEmpty()
                var i = 0
                for (l in prefList) {
                    if (locale.language == l.language && script == prefScript[i]) break
                    i++
                }
                if (i < prefList.size) {
                    val l = prefList[i]
                    val same_country = l.country == locale.country
                    ((++i) shl 4) or (if (same_country) {
                        if (locale.script == l.script) 0 else 2
                    } else {
                        (if (locale.country == prefCountry[i]) 0 else 1) or
                        (if (locale.script.isEmpty()) 0 else 2) or
                        (if (locale.country.isEmpty()) 0 else 4) or 8
                    })
                } else { -1 }
            }.also {
                cache.put(available_locale, it)
            }
        }

        companion object {
            val FALLBACK_LOCALE = Locale.ENGLISH
        }
    }
}
