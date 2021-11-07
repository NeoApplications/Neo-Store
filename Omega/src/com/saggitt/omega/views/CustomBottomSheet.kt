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

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.widget.EditText
import android.widget.TextView
import androidx.preference.PreferenceFragment
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.widget.WidgetsBottomSheet

class CustomBottomSheet @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : WidgetsBottomSheet(context, attrs, defStyleAttr) {

    private lateinit var mItemInfo: ItemInfo
    private var mEditTitle: EditText? = null
    private var mPreviousTitle: String? = null
    private var mForceOpen = false
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }

    private var mLauncher = Launcher.getLauncher(context)
    private var mFragmentManager = mLauncher.fragmentManager

    override fun populateAndShow(itemInfo: ItemInfo) {
        super.populateAndShow(itemInfo)
        mItemInfo = itemInfo
        val title = findViewById<TextView>(R.id.title)
        title.text = itemInfo.title
        (mFragmentManager.findFragmentById(R.id.sheet_prefs) as PrefsFragment).loadForApp(
            itemInfo, { setForceOpen() }, { unsetForceOpen() }) { reopen() }
        var allowTitleEdit = true

        /*if (itemInfo is ItemInfoWithIcon || mInfoProvider!!.supportsIcon()) {
            val icon = findViewById<ImageView>(R.id.icon)
            if (itemInfo is WorkspaceItemInfo && itemInfo.customIcon != null) {
                icon.setImageBitmap(itemInfo.customIcon)
            } else if (itemInfo is ItemInfoWithIcon) {
                icon.setImageBitmap(itemInfo.bitmap.icon)
            } else if (itemInfo is FolderInfo) {
                icon.setImageDrawable(itemInfo.getIcon(mLauncher))
                // Drawer folder
                if (itemInfo.container == ItemInfo.NO_ID) {
                    // TODO: Allow editing title for drawer folder & sync with group backend
                    allowTitleEdit = false
                }
            }
            if (mInfoProvider != null) {
                icon.setOnClickListener {
                    val editItem: ItemInfo? =
                        if (mItemInfo is FolderInfo && (mItemInfo as FolderInfo).isCoverMode) {
                            (mItemInfo as FolderInfo).coverInfo
                        } else {
                            mItemInfo
                        }
                    val editProvider = CustomInfoProvider.forItem<ItemInfo>(context, editItem)
                    if (editProvider != null) {
                        mLauncher.startEditIcon(editItem!!, editProvider)
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
        }*/
    }

    @SuppressLint("CommitTransaction")
    override fun onDetachedFromWindow() {
        val pf: Fragment? = mFragmentManager.findFragmentById(R.id.sheet_prefs)
        if (pf != null) {
            mFragmentManager.beginTransaction().remove(pf).commitAllowingStateLoss()
        }
        if (mEditTitle != null) {
            var newTitle: String? = mEditTitle!!.text.toString()
            if (newTitle != mPreviousTitle) {
                if (newTitle == "") newTitle = null
                /*mInfoProvider!!.setTitle(
                    mItemInfo, newTitle,
                    mLauncher.model.getWriter(false, true)
                )*/
                if (mItemInfo is ItemInfoWithIcon)
                    prefs.reloadApps
            }
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

    override fun setInsets(insets: Rect) {}

    class PrefsFragment : PreferenceFragment() {

        private lateinit var itemInfo: ItemInfo
        private var setForceOpen: Runnable? = null
        private var unsetForceOpen: Runnable? = null
        private var reopen: Runnable? = null

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