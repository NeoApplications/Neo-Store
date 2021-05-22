/*
 *  This file is part of Omega Launcher
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

package com.saggitt.omega.util

import android.app.WallpaperManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import com.saggitt.omega.BlankActivity

class FiveSecsProvider : ContentProvider() {
    private val PATH_RESET_5SEC_DELAY = "reset5secs"

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException("Unsupported")
    }

    override fun getType(uri: Uri): String? {
        // Not supported
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Unsupported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        checkCallingPackage()
        if (PATH_RESET_5SEC_DELAY == uri.lastPathSegment) {
            context!!.startActivity(Intent(context, BlankActivity::class.java))
            return 1
        }
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("Unsupported")
    }

    @Throws(SecurityException::class)
    private fun checkCallingPackage() {
        val callingPkg = callingPackage
        val info = WallpaperManager.getInstance(context).wallpaperInfo
        if (info != null) {
            if (info.packageName == callingPkg) return
        }
        if ("org.kustom.widget" == callingPkg) return
        throw SecurityException("Unauthorized")
    }
}