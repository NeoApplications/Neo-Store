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

package com.saggitt.omega.iconpack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.theme.ThemeOverride

class ApplyIconPackActivity : AppCompatActivity() {
    private val prefs by lazy { Utilities.getOmegaPrefs(this) }
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.SettingsTransparent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeOverride(themeSet, this).applyTheme(this)

        intent.getStringExtra("packageName")?.let {

            prefs.iconPacks.remove(it)
            prefs.iconPacks.add(0, it)
        }
        val packName = IconPackManager.getInstance(this).packList.currentPack().displayName
        val message = String.format(getString(R.string.icon_pack_applied_toast), packName)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
        Utilities.goToHome(this)
    }
}
