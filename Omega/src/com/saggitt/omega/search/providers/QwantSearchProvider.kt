/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.search.providers

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.saggitt.omega.util.isAppEnabled

@Keep
class QwantSearchProvider(context: Context) : FirefoxSearchProvider(context) {

    override val name = context.getString(R.string.search_provider_qwant)
    override val packageName = "com.qwant.liberty"

    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_qwant, null)!!

    override fun getPackage(context: Context) = listOf(
            "com.qwant.liberty"
    ).firstOrNull { context.packageManager.isAppEnabled(it, 0) }
}
