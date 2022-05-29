package com.looker.droidify.ui.compose.utils

import android.net.Uri
import com.looker.droidify.database.entity.Release
import com.looker.droidify.entity.PackageState
import com.looker.droidify.entity.ProductPreference
import com.looker.droidify.entity.Screenshot

interface Callbacks {
    fun onActionClick(action: PackageState?)
    fun onPreferenceChanged(preference: ProductPreference)
    fun onPermissionsClick(group: String?, permissions: List<String>)
    fun onScreenshotClick(screenshot: Screenshot)
    fun onReleaseClick(release: Release)
    fun onUriClick(uri: Uri, shouldConfirm: Boolean): Boolean
}