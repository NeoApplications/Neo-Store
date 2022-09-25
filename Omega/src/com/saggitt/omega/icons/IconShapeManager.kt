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

package com.saggitt.omega.icons

import android.content.Context
import android.graphics.Path
import android.graphics.Region
import android.graphics.drawable.AdaptiveIconDrawable
import android.text.TextUtils
import com.android.launcher3.Utilities
import com.android.launcher3.icons.GraphicsUtils
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.util.MainThreadInitializedObject

class IconShapeManager(private val context: Context) {
    val systemIconShape = getSystemShape()

    init {
        migratePref()
    }

    private fun migratePref() {
        val override = getLegacyValue()
        if (!TextUtils.isEmpty(override)) {
            try {
                Utilities.getPrefs(context).edit().remove(KEY_LEGACY_PREFERENCE).apply()
            } catch (_: RuntimeException) {
            }
        }
    }

    private fun getLegacyValue(): String {
        val devValue = Utilities.getDevicePrefs(context).getString(KEY_LEGACY_PREFERENCE, "")
        if (!TextUtils.isEmpty(devValue)) {
            Utilities.getPrefs(context).edit().putString(KEY_LEGACY_PREFERENCE, devValue).apply()
            Utilities.getDevicePrefs(context).edit().remove(KEY_LEGACY_PREFERENCE).apply()
        }

        return Utilities.getPrefs(context).getString(KEY_LEGACY_PREFERENCE, "")!!
    }

    private fun getSystemShape(): IconShape {
        if (!Utilities.ATLEAST_OREO) throw RuntimeException("not supported on < oreo")
        val iconMask = AdaptiveIconDrawable(null, null).iconMask
        val systemShape = findNearestShape(iconMask)
        return object : IconShape(systemShape) {

            override fun getMaskPath(): Path {
                return Path(iconMask)
            }

            override fun toString() = "system"

            override fun getHashString(): String {
                val resId = IconProvider.CONFIG_ICON_MASK_RES_ID
                if (resId == 0) {
                    return "system-path"
                }
                return context.getString(resId)
            }
        }
    }

    private fun findNearestShape(comparePath: Path): IconShape {
        val size = 200
        val clip = Region(0, 0, size, size)
        val iconR = Region().apply {
            setPath(comparePath, clip)
        }
        val shapePath = Path()
        val shapeR = Region()
        return listOf(
            IconShape.Circle,
            IconShape.Square,
            IconShape.RoundedSquare,
            IconShape.Squircle,
            IconShape.Sammy,
            IconShape.Teardrop,
            IconShape.Cylinder
        )
            .minByOrNull {
                shapePath.reset()
                it.addShape(shapePath, 0f, 0f, size / 2f)
                shapeR.setPath(shapePath, clip)
                shapeR.op(iconR, Region.Op.XOR)

                GraphicsUtils.getArea(shapeR)
            }!!
    }

    companion object {

        private const val KEY_LEGACY_PREFERENCE = "pref_override_icon_shape"

        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconShapeManager)

        fun getSystemIconShape(context: Context) = INSTANCE.get(context).systemIconShape
    }

}