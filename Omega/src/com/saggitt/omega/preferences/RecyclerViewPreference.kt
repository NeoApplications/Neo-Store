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

package com.saggitt.omega.preferences

import android.content.Context
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.saggitt.omega.theme.ThemeOverride

abstract class RecyclerViewPreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs) {

    private val themeRes = ThemeOverride.LauncherDialog().getTheme(context)
    protected val themedContext = ContextThemeWrapper(context, themeRes)

    init {
        dialogLayoutResource = R.layout.dialog_preference_recyclerview
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        onBindRecyclerView(view.findViewById(R.id.list))
    }

    abstract fun onBindRecyclerView(recyclerView: RecyclerView)
}
