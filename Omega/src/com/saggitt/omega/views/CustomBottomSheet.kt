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
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.touch.SingleAxisSwipeDetector
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.widget.WidgetsBottomSheet
import com.saggitt.omega.PREFS_FOLDER_COVER_MODE
import com.saggitt.omega.folder.CustomInfoProvider
import com.saggitt.omega.gestures.BlankGestureHandler
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.gestures.ui.LauncherGesturePreference
import com.saggitt.omega.preferences.OmegaPreferences

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
    private var mInfoProvider: CustomInfoProvider<ItemInfo>? = null

    override fun populateAndShow(itemInfo: ItemInfo) {
        super.populateAndShow(itemInfo)

        mItemInfo = itemInfo
        mInfoProvider = CustomInfoProvider.forItem(context, mItemInfo)

        val title = findViewById<TextView>(R.id.title)
        title.text = itemInfo.title
        (mFragmentManager.findFragmentById(R.id.sheet_prefs) as PrefsFragment).loadForApp(
            itemInfo, { setForceOpen() }, { unsetForceOpen() }) { reopen() }
        if (mInfoProvider!!.supportsIcon()) {
            val icon = findViewById<ImageView>(R.id.icon)
            if (itemInfo is FolderInfo) {
                icon.setImageDrawable(itemInfo.getIcon(mLauncher))
            }
        }

        if (mInfoProvider != null) {
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

        var newTitle: String? = mEditTitle!!.text.toString()
        if (newTitle != mPreviousTitle) {
            if (newTitle == "") newTitle = null
            mItemInfo.setTitle(newTitle, mLauncher.modelWriter)
        }
        mLauncher.modelWriter.updateItemInDatabase(mItemInfo)
        (mItemInfo as FolderInfo).onIconChanged()

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

    class PrefsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

        private var mSwipeUpPref: LauncherGesturePreference? = null
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

            val screen = preferenceScreen
            prefs = Utilities.getOmegaPrefs(activity)
            if (itemInfo !is FolderInfo) {
                mKey = ComponentKey(itemInfo.targetComponent, itemInfo.user)
            }
            mPrefCoverMode = findPreference(PREFS_FOLDER_COVER_MODE)!!
            mSwipeUpPref = screen.findPreference("pref_swipe_up_gesture")

            if (mProvider != null && mProvider!!.supportsSwipeUp(itemInfo)) {
                val previousSwipeUpAction = mProvider!!.getSwipeUpAction(itemInfo)
                mSwipeUpPref!!.value = previousSwipeUpAction
                mSwipeUpPref!!.onSelectHandler = { gestureHandler: GestureHandler ->
                    onSelectHandler(gestureHandler)
                }
            }

            if (itemInfo is FolderInfo) {
                mPrefCoverMode.isChecked = (itemInfo as FolderInfo).isCoverMode
            }
        }

        override fun onDetach() {
            super.onDetach()
            if (mProvider != null && selectedHandler != null) {
                val stringValue = selectedHandler.toString()
                val provider = CustomInfoProvider.forItem<ItemInfo>(
                    activity!!, itemInfo
                )
                provider?.setSwipeUpAction(itemInfo, stringValue)
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
                startBackupResult.launch(handler.configIntent)
            } else {
                updatePref()
            }
        }

        private val startBackupResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val resultData = result.data
                    if (resultData!!.data != null) {
                        selectedHandler!!.onConfigResult(resultData)
                        updatePref()
                    }
                } else {
                    selectedHandler = previousHandler
                }
                reopen!!.run()
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
            return true
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
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