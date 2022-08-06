package com.saggitt.omega.preferences.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.getIcon
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class FeedPreference(context: Context, attrs: AttributeSet?) :
    ListPreference(context, attrs) {
    val config = Config(context)
    private val prefs = Utilities.getOmegaPrefs(context)
    private val feeds = listOf(
        ProviderInfo(context.getString(R.string.none), "", context.getIcon())
    ) +
            config.feedProviderList(context).map {
                ProviderInfo(
                    it.loadLabel(context.packageManager).toString(),
                    it.packageName,
                    it.loadIcon(context.packageManager)
                )
            }

    private val current
        get() = feeds.firstOrNull { it.packageName == prefs.feedProvider.onGetValue() }
            ?: feeds[0]

    init {
        entries = feeds.map { it.displayName }.toTypedArray()
        entryValues = feeds.map { it.packageName }.toTypedArray()
        updateSummary()
    }

    private fun updateSummary() {
        summary = current.displayName
        icon = current.icon
    }

    override fun callChangeListener(newValue: Any?): Boolean {
        MainScope().launch { updateSummary() }
        return super.callChangeListener(newValue)
    }
}

data class ProviderInfo(
    val displayName: String,
    val packageName: String,
    val icon: Drawable?
)