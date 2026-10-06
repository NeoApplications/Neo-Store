package com.machiav3lli.fdroid.utils

import androidx.core.os.LocaleListCompat
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.PREFS_LANGUAGE_DEFAULT
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
}
