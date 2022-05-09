/*
 *  This file is part of Omega Launcher.
 *  Copyright (c) 2021   Saul Henriquez
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

import android.app.Activity
import android.app.FragmentManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.SwitchPreference
import com.android.launcher3.*
import com.android.launcher3.model.data.*
import com.android.launcher3.touch.SingleAxisSwipeDetector
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.PackageManagerHelper
import com.android.launcher3.widget.WidgetsBottomSheet
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.PREFS_APP_HIDE
import com.saggitt.omega.PREFS_APP_SHOW_IN_TABS
import com.saggitt.omega.PREFS_FOLDER_COVER_MODE
import com.saggitt.omega.allapps.CustomAppFilter
import com.saggitt.omega.gestures.BlankGestureHandler
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.gestures.OmegaShortcutActivity.Companion.REQUEST_CODE
import com.saggitt.omega.gestures.ui.LauncherGesturePreference
import com.saggitt.omega.override.CustomInfoProvider
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.preferences.custom.MultiSelectTabPreference

class CustomBottomSheet @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : WidgetsBottomSheet(context, attrs, defStyleAttr) {

    private var mEditTitle: EditText? = null
    private var mPreviousTitle: String? = null
    private lateinit var mItemInfo: ItemInfo
    private var mForceOpen = false
    private var mLauncher = Launcher.getLauncher(context)
    private val mFragmentManager: FragmentManager = mLauncher.fragmentManager
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }

    private var mInfoProvider: CustomInfoProvider<ItemInfo>? = null

    override fun populateAndShow(itemInfo: ItemInfo) {
        super.populateAndShow(itemInfo)

        mItemInfo = itemInfo
        mInfoProvider = CustomInfoProvider.forItem(context, mItemInfo)

        val title = findViewById<TextView>(R.id.title)
        title.text = itemInfo.title
        (mFragmentManager.findFragmentById(R.id.sheet_prefs) as PrefsFragment).loadForApp(
            itemInfo, { setForceOpen() }, { unsetForceOpen() }) { reopen() }
        var allowTitleEdit = true
        if (itemInfo is ItemInfoWithIcon || mInfoProvider!!.supportsIcon()) {
            val icon = findViewById<ImageView>(R.id.icon)
            if (itemInfo is WorkspaceItemInfo && itemInfo.customIcon != null) {
                icon.setImageBitmap(itemInfo.customIcon)
            } else if (itemInfo is ItemInfoWithIcon) {
                icon.setImageBitmap(itemInfo.bitmap.icon)
            } else if (itemInfo is FolderInfo) {
                icon.setImageDrawable(itemInfo.getIcon(mLauncher))
                // Drawer folder
                if (itemInfo.container == ItemInfo.NO_ID) {
                    allowTitleEdit = false
                }
            }

            Log.d("CustomBottomSheet", "Provider is null: ${mInfoProvider == null}")
            if (mInfoProvider != null) {
                val launcher = OmegaLauncher.getLauncher(context)
                icon.setOnClickListener {
                    val editItem: ItemInfo? =
                        if (mItemInfo is FolderInfo && (mItemInfo as FolderInfo).isCoverMode) {
                            (mItemInfo as FolderInfo).coverInfo
                        } else {
                            mItemInfo
                        }
                    val editProvider = CustomInfoProvider.forItem<ItemInfo>(context, editItem)
                    if (editProvider != null) {
                        launcher.startEditIcon(editItem!!, editProvider)
                    }
                }
            }
        }

        if (mInfoProvider != null && allowTitleEdit) {
            mPreviousTitle = mInfoProvider!!.getCustomTitle(mItemInfo)
            if (mPreviousTitle == null) mPreviousTitle = ""
            mEditTitle = findViewById(R.id.edit_title)
            mEditTitle?.let {
                it.hint = mInfoProvider!!.getDefaultTitle(mItemInfo)
                it.setText(mPreviousTitle)
                it.visibility = VISIBLE
            }
            title.visibility = GONE
        }
    }

    override fun hasSeenEducationTip(): Boolean {
        return true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mContent = findViewById(R.id.widgets_bottom_sheet)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t

        // Content is laid out as center bottom aligned.
        val contentWidth = mContent.measuredWidth
        val contentLeft = (width - contentWidth - mInsets.left - mInsets.right) / 2 + mInsets.left
        mContent.layout(
            contentLeft, height - mContent.measuredHeight,
            contentLeft + contentWidth, height
        )
        setTranslationShift(mTranslationShift)

    }

    override fun updateMaxSpansPerRow(): Boolean {
        return false
    }

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        val directionsToDetectScroll =
            if (mSwipeDetector.isIdleState) SingleAxisSwipeDetector.DIRECTION_NEGATIVE else 0
        mSwipeDetector.setDetectableScrollConditions(
            directionsToDetectScroll, false
        )
        mSwipeDetector.onTouchEvent(ev)
        return (mSwipeDetector.isDraggingOrSettling
                || !popupContainer.isEventOverView(mContent, ev))
    }

    public override fun onDetachedFromWindow() {
        val pf = mFragmentManager.findFragmentById(R.id.sheet_prefs)
        if (pf != null) {
            mFragmentManager.beginTransaction().remove(pf).commitAllowingStateLoss()
        }

        if (mEditTitle != null && mItemInfo !is FolderInfo) {
            val componentKey = ComponentKey(mItemInfo.targetComponent, mItemInfo.user)
            var newTitle: String? = mEditTitle!!.text.toString()
            if (newTitle != mPreviousTitle) {
                if (newTitle == "") newTitle = null
                prefs.customAppName[componentKey] = newTitle
                val las = LauncherAppState.getInstance(context)
                las.iconCache.updateIconsForPkg(
                    componentKey.componentName.packageName,
                    componentKey.user
                )
                if (mItemInfo is ItemInfoWithIcon)
                    prefs.reloadApps
            }
        } else {
            var newTitle: String? = mEditTitle!!.text.toString()
            if (newTitle != mPreviousTitle) {
                if (newTitle == "") newTitle = null
                mItemInfo.setTitle(newTitle, mLauncher.modelWriter)
            }
            mLauncher.modelWriter.updateItemInDatabase(mItemInfo)
            (mItemInfo as FolderInfo).onIconChanged()
        }

        super.onDetachedFromWindow()
    }

    override fun handleClose(animate: Boolean, defaultDuration: Long) {
        if (mForceOpen) return
        super.handleClose(animate, defaultDuration)
    }

    private fun setForceOpen() {
        mForceOpen = true
    }

    private fun unsetForceOpen() {
        mForceOpen = false
    }

    private fun reopen() {
        mForceOpen = false
        mIsOpen = true
        mLauncher.dragLayer.onViewAdded(this)
    }

    override fun onWidgetsBound() {}

    class PrefsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

        private var mSwipeUpPref: LauncherGesturePreference? = null
        private var mTabsPref: MultiSelectTabPreference? = null
        private lateinit var prefs: OmegaPreferences
        private lateinit var mPrefCoverMode: SwitchPreference
        private var mKey: ComponentKey? = null
        private lateinit var itemInfo: ItemInfo
        private var previousHandler: GestureHandler? = null
        private var selectedHandler: GestureHandler? = null
        private var setForceOpen: Runnable? = null
        private var unsetForceOpen: Runnable? = null
        private var reopen: Runnable? = null
        private var mProvider: CustomInfoProvider<ItemInfo>? = null

        @Deprecated("Deprecated in Java")
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
            val isApp =
                itemInfo is AppInfo || itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
            val screen = preferenceScreen
            prefs = Utilities.getOmegaPrefs(activity)
            if (itemInfo !is FolderInfo) {
                mKey = ComponentKey(itemInfo.targetComponent, itemInfo.user)
            }
            val mPrefHide = findPreference<SwitchPreference>(PREFS_APP_HIDE)
            mPrefCoverMode = findPreference(PREFS_FOLDER_COVER_MODE)!!
            mSwipeUpPref = screen.findPreference("pref_swipe_up_gesture")
            mTabsPref = findPreference(PREFS_APP_SHOW_IN_TABS)
            if (isApp) {
                mPrefHide!!.isChecked = CustomAppFilter.isHiddenApp(context, mKey)
                mPrefHide.onPreferenceChangeListener = this
            } else {
                screen.removePreference(mPrefHide!!)
            }
            if (!isApp || !prefs.drawerTabs.isEnabled) {
                screen.removePreference(mTabsPref!!)
            } else {
                mTabsPref!!.componentKey = mKey!!
                mTabsPref!!.activity = activity
                mTabsPref!!.updateSummary()
            }
            if (mProvider != null && mProvider!!.supportsSwipeUp(itemInfo)) {
                val previousSwipeUpAction = mProvider!!.getSwipeUpAction(itemInfo)
                mSwipeUpPref!!.value = previousSwipeUpAction
                mSwipeUpPref!!.onSelectHandler = { gestureHandler: GestureHandler ->
                    onSelectHandler(gestureHandler)
                }
            } else {
                preferenceScreen.removePreference(mSwipeUpPref!! as Preference)
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
                preferenceScreen.removePreference(preferenceScreen.findPreference("debug")!!)
            }
            if (itemInfo is FolderInfo) {
                mPrefCoverMode.isChecked = (itemInfo as FolderInfo).isCoverMode
            } else {
                mPrefCoverMode.let { preferenceScreen.removePreference(it) }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onDetach() {
            super.onDetach()
            if (mProvider != null && selectedHandler != null) {
                val stringValue = selectedHandler.toString()
                val provider = CustomInfoProvider.forItem<ItemInfo>(
                    activity, itemInfo
                )
                provider?.setSwipeUpAction(itemInfo, stringValue)
            }
            if (mTabsPref!!.edited) {
                prefs.drawerTabs.saveToJson()
            }
            if (itemInfo is FolderInfo) {
                val folderInfo = itemInfo as FolderInfo
                val coverEnabled = mPrefCoverMode.isChecked
                if (folderInfo.isCoverMode != coverEnabled) {
                    val launcher = Launcher.getLauncher(activity)
                    folderInfo.setCoverMode(coverEnabled, launcher.modelWriter)
                    folderInfo.onIconChanged()
                }
            }
        }

        private fun onSelectHandler(handler: GestureHandler) {
            previousHandler = selectedHandler
            selectedHandler = handler
            if (handler.configIntent != null) {
                setForceOpen!!.run()
                startActivityForResult(handler.configIntent, REQUEST_CODE)
            } else {
                updatePref()
            }
        }

        @Deprecated("")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            if (requestCode == REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    selectedHandler!!.onConfigResult(data)
                    updatePref()
                } else {
                    selectedHandler = previousHandler
                }
                reopen!!.run()
            } else {
                unsetForceOpen!!.run()
            }
        }

        private fun updatePref() {
            if (mProvider != null && selectedHandler != null) {
                setForceOpen!!.run()
                val stringValue: String? = if (selectedHandler is BlankGestureHandler) {
                    null
                } else {
                    selectedHandler.toString()
                }
                mSwipeUpPref!!.value = stringValue
                unsetForceOpen!!.run()
            }
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val enabled = newValue as Boolean
            val launcher = Launcher.getLauncher(activity)
            when (preference.key) {
                PREFS_APP_HIDE -> CustomAppFilter.setComponentNameState(
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
    }

    companion object {
        @JvmStatic
        fun show(launcher: Launcher, itemInfo: ItemInfo) {
            val cbs = launcher.layoutInflater
                .inflate(
                    R.layout.app_edit_bottom_sheet,
                    launcher.dragLayer,
                    false
                ) as CustomBottomSheet
            cbs.populateAndShow(itemInfo)
        }
    }
}