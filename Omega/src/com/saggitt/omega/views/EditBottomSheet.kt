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

package com.saggitt.omega.views

import android.annotation.SuppressLint
import android.app.FragmentManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.SwitchPreference
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.PackageManagerHelper
import com.saggitt.omega.allapps.CustomAppFilter
import com.saggitt.omega.preferences.OmegaPreferences

@SuppressLint("ViewConstructor")
class EditBottomSheet(context: Context, mItemInfo: ItemInfo) : LinearLayoutCompat(context) {

    private lateinit var itemInfo: ItemInfo
    private var mEditTitle: EditText? = null
    private var mPreviousTitle: String? = null
    private var mForceOpen = false
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }
    private val mFragmentManager: FragmentManager = Launcher.getLauncher(context).fragmentManager

    init {
        View.inflate(context, R.layout.app_edit_bottom_sheet, this)
        itemInfo = mItemInfo
        val title = findViewById<TextView>(R.id.title)
        title.text = itemInfo.title
        var allowTitleEdit = true

        if (itemInfo is ItemInfoWithIcon) {

            val icon = findViewById<ImageView>(R.id.icon)
            if (itemInfo is WorkspaceItemInfo) {
                icon.setImageBitmap((itemInfo as WorkspaceItemInfo).bitmap.icon)
            } else if (itemInfo is ItemInfoWithIcon) {
                icon.setImageBitmap((itemInfo as ItemInfoWithIcon).bitmap.icon)
            }
        }

        (mFragmentManager.findFragmentById(R.id.sheet_prefs) as PrefsFragment).loadForApp(
            itemInfo, { setForceOpen() }, { unsetForceOpen() }) { reopen() }
    }

    private fun setForceOpen() {
        mForceOpen = true
    }

    private fun unsetForceOpen() {
        mForceOpen = false
    }

    private fun reopen() {
        mForceOpen = false
    }

    class PrefsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

        private lateinit var itemInfo: ItemInfo
        private var setForceOpen: Runnable? = null
        private var unsetForceOpen: Runnable? = null
        private var reopen: Runnable? = null
        private var mKey: ComponentKey? = null
        private lateinit var prefs: OmegaPreferences

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.app_edit_prefs, rootKey)
        }

        fun loadForApp(
            info: ItemInfo,
            setForceOpen: Runnable?,
            unsetForceOpen: Runnable?,
            reopen: Runnable?
        ) {
            itemInfo = info
            this.setForceOpen = setForceOpen
            this.unsetForceOpen = unsetForceOpen
            this.reopen = reopen

            val context: Context = activity
            prefs = Utilities.getOmegaPrefs(activity)

            val isApp =
                itemInfo is AppInfo || itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
            val screen = preferenceScreen

            mKey = ComponentKey(itemInfo.targetComponent, itemInfo.user)

            val mPrefHide = findPreference<SwitchPreference>(PREF_HIDE)
            if (isApp) {
                mPrefHide!!.isChecked = CustomAppFilter.isHiddenApp(context, mKey)
                mPrefHide.onPreferenceChangeListener = this
            } else {
                screen.removePreference(mPrefHide)
            }

            if (prefs.showDebugInfo && mKey != null && mKey!!.componentName != null) {
                val componentPref = preferenceScreen.findPreference<Preference>("componentName")
                val versionPref = preferenceScreen.findPreference<Preference>("versionName")
                componentPref!!.onPreferenceClickListener = this
                versionPref!!.onPreferenceClickListener = this
                componentPref.summary = mKey.toString()
                versionPref.summary =
                    PackageManagerHelper(context).getPackageVersion(mKey!!.componentName.packageName)
            } else {
                preferenceScreen.removePreference(preferenceScreen.findPreference("debug"))
            }
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val enabled = newValue as Boolean
            val launcher = Launcher.getLauncher(activity)
            when (preference.key) {
                PREF_HIDE -> CustomAppFilter.setComponentNameState(
                    launcher,
                    mKey.toString(),
                    enabled
                )
            }
            return true
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            when (preference.key) {
                "componentName", "versionName" -> {
                    val clipboard =
                        activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(
                        getString(R.string.debug_component_name),
                        preference.summary
                    )
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        activity,
                        R.string.debug_component_name_copied,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return true
        }

        companion object {
            private const val PREF_HIDE = "pref_app_hide"
        }
    }

    companion object {
        fun show(launcher: Launcher, mItemInfo: ItemInfo, animate: Boolean) {
            val sheet = CustomBottomSheet.inflate(launcher)
            val view = EditBottomSheet(launcher, mItemInfo)
            sheet.show(view, animate)
        }
    }
}