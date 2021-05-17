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
package com.saggitt.omega.icons

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.adaptive.IconShapeCustomizeView
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.theme.ThemedContextProvider
import com.saggitt.omega.util.applyColor

class IconCustomizeFragment : Fragment() {
    private var coloredView: View? = null
    private var shapeLessView: View? = null
    private var legacyView: View? = null
    private var whiteView: View? = null
    private var adaptiveView: View? = null
    private lateinit var prefs: OmegaPreferences
    private var coloredIcons = false
    private var shapeLess = false
    private var legacy = false
    private var white = false
    private var adaptive = false
    private var adapter: IconShapeAdapter? = null
    private var customizeView: IconShapeCustomizeView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_icon_customization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mContext: Context? = activity

        //Load Preferences
        prefs = Utilities.getOmegaPrefs(mContext)
        coloredIcons = prefs.colorizedLegacyTreatment
        shapeLess = prefs.forceShapeless
        legacy = prefs.enableLegacyTreatment
        white = prefs.enableWhiteOnlyTreatment
        adaptive = prefs.adaptifyIconPacks

        //Load Shapes
        val shapeView: RecyclerView = view.findViewById(R.id.shape_view)
        shapeView.layoutManager = GridLayoutManager(mContext, 4)
        adapter = IconShapeAdapter(requireContext())
        shapeView.adapter = adapter

        //Load switch preferences
        coloredView = view.findViewById(R.id.colored_icons)
        shapeLessView = view.findViewById(R.id.shapeless_icons)
        legacyView = view.findViewById(R.id.legacy_icons)
        whiteView = view.findViewById(R.id.white_icons)
        adaptiveView = view.findViewById(R.id.adaptive_icons)

        //setup switch preferences
        setupSwitchView(shapeLessView, shapeLess)
        setupSwitchView(legacyView, legacy)
        setupSwitchView(whiteView, white)
        setupSwitchView(adaptiveView, adaptive)
        setupSwitchView(coloredView, coloredIcons)
        hideViews()
        customizeView = view.findViewById(R.id.customizeView)
    }

    fun showDialog() {
        val themedContext =
            ThemedContextProvider(requireContext(), null, ThemeOverride.Settings()).get()
        val dialog = AlertDialog.Builder(
            themedContext, ThemeOverride.AlertDialog().getTheme(
                requireContext()
            )
        )
        dialog.setTitle(R.string.menu_icon_shape)
        dialog.setView(R.layout.icon_shape_customize_view)
        dialog.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            DialogInterface.OnClickListener { dialog, _ ->
                prefs.iconShape = customizeView!!.currentShape.getHashString()
                adapter!!.notifyDataSetChanged()
                dialog.dismiss()
            }
        }
        dialog.setNegativeButton(android.R.string.cancel, null)
        dialog.create()
        dialog.show()
    }

    /*
     * Hidde options when the android version is lower than oreo
     * */
    private fun hideViews() {
        if (!Utilities.ATLEAST_OREO) {
            coloredView!!.visibility = View.GONE
            shapeLessView!!.visibility = View.GONE
            legacyView!!.visibility = View.GONE
            whiteView!!.visibility = View.GONE
            adaptiveView!!.visibility = View.GONE
        }
    }

    /*
     * Sync switch view according to the preference state.
     * */
    private fun setupSwitchView(itemView: View?, isChecked: Boolean) {
        val switchView = itemView!!.findViewById<Switch>(R.id.switchWidget)
        switchView.applyColor(prefs.accentColor)
        syncSwitch(switchView, isChecked)
        itemView.setOnClickListener { view: View -> performClick(view, switchView) }
    }

    fun performClick(view: View, switchView: Switch) {
        if (view === coloredView) {
            coloredIcons = !coloredIcons
            syncSwitch(switchView, coloredIcons)
            prefs.colorizedLegacyTreatment = coloredIcons
            updateWhite(coloredIcons)
        } else if (view === shapeLessView) {
            shapeLess = !shapeLess
            syncSwitch(switchView, shapeLess)
            prefs.forceShapeless = shapeLess
        } else if (view === legacyView) {
            legacy = !legacy
            syncSwitch(switchView, legacy)
            prefs.enableLegacyTreatment = legacy
            if (!legacy) {
                updateColoredBackground(false)
                updateAdaptive(false)
                updateWhite(false)
            } else {
                updateColoredBackground(true)
                updateAdaptive(true)
                updateWhite(true)
            }
        } else if (view === whiteView) {
            white = !white
            syncSwitch(switchView, white)
            prefs.enableWhiteOnlyTreatment = white
        } else if (view === adaptiveView) {
            adaptive = !adaptive
            syncSwitch(switchView, adaptive)
            prefs.adaptifyIconPacks = adaptive
        }
    }

    private fun updateColoredBackground(state: Boolean) {
        if (!state) {
            coloredView!!.isClickable = false
            prefs.colorizedLegacyTreatment = false
            coloredView!!.findViewById<View>(R.id.switchWidget).isEnabled = false
        } else {
            coloredView!!.isClickable = true
            coloredView!!.findViewById<View>(R.id.switchWidget).isEnabled =
                true
        }
    }

    private fun updateWhite(state: Boolean) {
        if (!state) {
            whiteView!!.isClickable = false
            prefs.enableWhiteOnlyTreatment = false
            whiteView!!.findViewById<View>(R.id.switchWidget).isEnabled =
                false
        } else {
            whiteView!!.isClickable = true
            whiteView!!.findViewById<View>(R.id.switchWidget).isEnabled = true
        }
    }

    private fun updateAdaptive(state: Boolean) {
        if (!state) {
            adaptiveView!!.isClickable = false
            prefs.adaptifyIconPacks = false
            adaptiveView!!.findViewById<View>(R.id.switchWidget).isEnabled = false
        } else {
            adaptiveView!!.isClickable = true
            adaptiveView!!.findViewById<View>(R.id.switchWidget).isEnabled =
                true
        }
    }

    private fun syncSwitch(switchCompat: Switch, checked: Boolean) {
        switchCompat.isChecked = checked
    }
}