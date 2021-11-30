/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences.custom

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.android.launcher3.BuildConfig
import com.saggitt.omega.util.Config
import java.util.*

class LanguagePreference(context: Context, attrs: AttributeSet?) :
    ListPreference(context, attrs) {

    private var defaultLanguageName = "System"
    private var defaultLanguageCode = "en"

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        setDefaultValue("")

        val langCodes = BuildConfig.DETECTED_ANDROID_LOCALES
        val languages: ArrayList<String> = ArrayList()
        val contextUtils = Config(context)

        if (langCodes.isNotEmpty()) {
            for (langId in langCodes) {
                val locale: Locale = contextUtils.getLocaleByAndroidCode(langId)
                languages.add(summarizeLocale(locale, langId) + ";" + langId)
            }
        }

        // Sort languages naturally
        languages.sort()

        val mEntries = arrayOfNulls<String>(languages.size + 2)
        val mEntryValues = arrayOfNulls<String>(languages.size + 2)

        for (i in languages.indices) {
            mEntries[i + 2] = languages[i].split(";").toTypedArray()[0]
            mEntryValues[i + 2] = languages[i].split(";").toTypedArray()[1]
        }

        mEntryValues[0] = ""
        mEntries[0] = "$defaultLanguageName » " + summarizeLocale(
            context.resources
                .configuration.locales.get(0), ""
        )

        mEntryValues[1] = defaultLanguageCode
        mEntries[1] = summarizeLocale(
            contextUtils.getLocaleByAndroidCode(defaultLanguageCode), defaultLanguageName
        )

        entries = mEntries
        entryValues = mEntryValues
    }

    // Concat english and localized language name
    // Append country if country specific (e.g. Portuguese Brazil)
    private fun summarizeLocale(locale: Locale, localeAndroidCode: String): String {
        val country = locale.getDisplayCountry(locale)
        val language = locale.getDisplayLanguage(locale)
        var ret = (locale.getDisplayLanguage(Locale.ENGLISH)
            .toString() + " (" + language.substring(0, 1)
            .uppercase(Locale.getDefault()) + language.substring(1)
                + (if (country.isNotEmpty() && country.lowercase(Locale.getDefault()) != language.lowercase(
                Locale.getDefault()
            )
        ) ", $country" else "")
                + ")")
        if (localeAndroidCode == "zh-rCN") {
            ret = ret.substring(
                0,
                ret.indexOf(" ") + 1
            ) + "Simplified" + ret.substring(ret.indexOf(" "))
        } else if (localeAndroidCode == "zh-rTW") {
            ret = ret.substring(
                0,
                ret.indexOf(" ") + 1
            ) + "Traditional" + ret.substring(ret.indexOf(" "))
        }
        return ret
    }

    // Add current language to summary
    override fun getSummary(): CharSequence {
        val locale = Config(context).getLocaleByAndroidCode(value)
        var prefix = ""
        if (!TextUtils.isEmpty(super.getSummary())) {
            prefix = super.getSummary().toString() + "\n"
        }

        return prefix + summarizeLocale(locale, value)
    }

    override fun callChangeListener(newValue: Any?): Boolean {
        if (newValue is String) {
            // Does not apply to existing UI, use recreate()
            Config(context).setAppLanguage((newValue as String?)!!)
        }
        return super.callChangeListener(newValue)
    }

}