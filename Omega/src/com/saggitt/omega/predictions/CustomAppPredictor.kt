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
package com.saggitt.omega.predictions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import android.util.Log
import android.view.View
import com.android.launcher3.AppFilter
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.AllAppsContainerView
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.appprediction.ComponentKeyMapper
import com.android.launcher3.appprediction.DynamicItemCache
import com.android.launcher3.logging.UserEventDispatcher
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.settings.SettingsActivity
import java.util.*

class CustomAppPredictor(private val mContext: Context) : UserEventDispatcher(),
    OnSharedPreferenceChangeListener, AllAppsStore.OnUpdateListener {
    private val mAppFilter: AppFilter = AppFilter.newInstance(mContext)
    private val mPrefs: SharedPreferences = Utilities.getPrefs(mContext)
    private val mPackageManager: PackageManager = mContext.packageManager
    private val mDynamicItemCache: DynamicItemCache = DynamicItemCache(mContext) { onAppsUpdated() }
    private val mUiManager: UiManager = UiManager(this)

    init {
        mPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onAppsUpdated() {
        dispatchOnChange(false)
    }

    private fun dispatchOnChange(changed: Boolean) {}
    val predictions: List<ComponentKeyMapper?>
        get() {
            var list: MutableList<ComponentKeyMapper?> = ArrayList()
            if (isPredictorEnabled) {
                clearNonExistentPackages()
                val predictionList: MutableList<String> = ArrayList(
                    stringSetCopy
                )
                predictionList.sortWith { o1: String, o2: String ->
                    getLaunchCount(o2).compareTo(getLaunchCount(o1))
                }
                for (prediction in predictionList) {
                    Log.d("CustomAppPredictor", "Loading $prediction")
                    list.add(getComponentFromString(prediction))
                }
                if (list.size < MAX_PREDICTIONS) {
                    for (placeHolder in PLACE_HOLDERS) {
                        val intent = mPackageManager.getLaunchIntentForPackage(placeHolder)
                        if (intent != null) {
                            val componentInfo = intent.component
                            if (componentInfo != null) {
                                val key = ComponentKey(componentInfo, Process.myUserHandle())
                                if (!predictionList.contains(key.toString())) {
                                    list.add(ComponentKeyMapper(key, mDynamicItemCache))
                                }
                            }
                        }
                    }
                }
                if (list.size > MAX_PREDICTIONS) {
                    list = list.subList(0, MAX_PREDICTIONS)
                }
            }
            return list
        }

    override fun logAppLaunch(v: View, intent: Intent, user: UserHandle?) {
        super.logAppLaunch(v, intent, user)
        if (isPredictorEnabled && recursiveIsDrawer(v)) {
            val componentInfo = intent.component
            if (componentInfo != null && mAppFilter.shouldShowApp(
                    componentInfo,
                    Process.myUserHandle()
                )
            ) {
                clearNonExistentPackages()
                val predictionSet = stringSetCopy
                val edit = mPrefs.edit()
                val prediction = ComponentKey(componentInfo, user).toString()
                if (predictionSet.contains(prediction)) {
                    edit.putInt(
                        PREDICTION_PREFIX + prediction,
                        getLaunchCount(prediction) + BOOST_ON_OPEN
                    )
                } else if (predictionSet.size < MAX_PREDICTIONS || decayHasSpotFree(
                        predictionSet,
                        edit
                    )
                ) {
                    predictionSet.add(prediction)
                }
                edit.putStringSet(PREDICTION_SET, predictionSet)
                edit.apply()
                mUiManager.onPredictionsUpdated()
            }
        }
    }

    private fun decayHasSpotFree(
        toDecay: MutableSet<String>,
        edit: SharedPreferences.Editor
    ): Boolean {
        var spotFree = false
        val toRemove: MutableSet<String> = HashSet()
        for (prediction in toDecay) {
            var launchCount = getLaunchCount(prediction)
            if (launchCount > 0) {
                edit.putInt(PREDICTION_PREFIX + prediction, --launchCount)
            } else if (!spotFree) {
                edit.remove(PREDICTION_PREFIX + prediction)
                toRemove.add(prediction)
                spotFree = true
            }
        }
        for (prediction in toRemove) {
            toDecay.remove(prediction)
        }
        return spotFree
    }

    /**
     * Zero-based launch count of a shortcut
     *
     * @param component serialized component
     * @return the number of launches, at least zero
     */
    private fun getLaunchCount(component: String): Int {
        return mPrefs.getInt(PREDICTION_PREFIX + component, 0)
    }

    private fun recursiveIsDrawer(v: View?): Boolean {
        if (v != null) {
            var parent = v.parent
            while (parent != null) {
                if (parent is AllAppsContainerView) {
                    return true
                }
                parent = parent.parent
            }
        }
        return false
    }

    private val isPredictorEnabled: Boolean
        get() = Utilities.getPrefs(mContext)
            .getBoolean(SettingsActivity.SHOW_PREDICTIONS_PREF, true)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SettingsActivity.SHOW_PREDICTIONS_PREF) {
            if (!isPredictorEnabled) {
                val predictionSet: Set<String> = stringSetCopy
                val edit = mPrefs.edit()
                for (prediction in predictionSet) {
                    Log.i(
                        "Predictor",
                        "Clearing " + prediction + " at " + getLaunchCount(prediction)
                    )
                    edit.remove(PREDICTION_PREFIX + prediction)
                }
                edit.putStringSet(PREDICTION_SET, EMPTY_SET)
                edit.apply()
            }
            mUiManager.onPredictionsUpdated()
        } else if (key == HIDDEN_PREDICTIONS_SET_PREF) {
            mUiManager.onPredictionsUpdated()
        }
    }

    private fun getComponentFromString(str: String?): ComponentKeyMapper {
        return ComponentKeyMapper(
            ComponentKey(
                ComponentName(mContext, str!!),
                Process.myUserHandle()
            ), mDynamicItemCache
        )
    }

    private fun clearNonExistentPackages() {
        val originalSet = mPrefs.getStringSet(PREDICTION_SET, EMPTY_SET)
        val predictionSet: MutableSet<String> = HashSet(originalSet)
        val edit = mPrefs.edit()
        for (prediction in originalSet!!) {
            try {
                mPackageManager.getPackageInfo(
                    ComponentKey(
                        ComponentName(mContext, prediction),
                        Process.myUserHandle()
                    ).componentName.packageName, 0
                )
            } catch (e: PackageManager.NameNotFoundException) {
                predictionSet.remove(prediction)
                edit.remove(PREDICTION_PREFIX + prediction)
            }
        }
        edit.putStringSet(PREDICTION_SET, predictionSet)
        edit.apply()
    }

    private val stringSetCopy: MutableSet<String>
        get() = HashSet(mPrefs.getStringSet(PREDICTION_SET, EMPTY_SET))

    class UiManager(private val mPredictor: CustomAppPredictor) {
        private val mListeners: MutableList<Listener> = ArrayList()
        fun addListener(listener: Listener) {
            mListeners.add(listener)
            listener.onPredictionsUpdated()
        }

        fun removeListener(listener: Listener) {
            mListeners.remove(listener)
        }

        val isEnabled: Boolean
            get() = mPredictor.isPredictorEnabled
        val predictions: List<ComponentKeyMapper?>
            get() = mPredictor.predictions

        fun onPredictionsUpdated() {
            for (listener in mListeners) {
                listener.onPredictionsUpdated()
            }
        }

        interface Listener {
            fun onPredictionsUpdated()
        }
    }

    companion object {
        private const val BOOST_ON_OPEN = 9
        private const val PREDICTION_SET = "pref_prediction_set"
        private const val PREDICTION_PREFIX = "pref_prediction_count_"
        private const val HIDDEN_PREDICTIONS_SET_PREF = "pref_hidden_prediction_set"
        private val EMPTY_SET: Set<String> = HashSet()
        val PLACE_HOLDERS = arrayOf(
            "com.google.android.apps.photos",
            "com.google.android.apps.maps",
            "com.google.android.gm",
            "com.google.android.deskclock",
            "com.google.android.youtube",
            "com.android.settings",
            "com.whatsapp",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.yodo1.crossyroad",
            "com.spotify.music",
            "com.android.chrome",
            "com.instagram.android",
            "com.skype.raider",
            "com.snapchat.android",
            "com.viber.voip",
            "com.twitter.android",
            "com.android.phone",
            "com.google.android.music",
            "com.google.android.calendar",
            "com.google.android.apps.genie.geniewidget",
            "com.netflix.mediaclient",
            "bbc.iplayer.android",
            "com.google.android.videos",
            "com.amazon.mShop.android.shopping",
            "com.microsoft.office.word",
            "com.google.android.apps.docs",
            "com.google.android.keep",
            "com.google.android.apps.plus",
            "com.google.android.talk"
        )
        private const val MAX_PREDICTIONS = 12
        fun setComponentNameState(context: Context, key: ComponentKey, hidden: Boolean) {
            val comp = key.toString()
            val hiddenApps = getHiddenApps(context)
            while (hiddenApps.contains(comp)) {
                hiddenApps.remove(comp)
            }
            if (hidden) {
                hiddenApps.add(comp)
            }
            setHiddenApps(context, hiddenApps)
        }

        fun isHiddenApp(context: Context, key: ComponentKey): Boolean {
            return getHiddenApps(context).contains(key.toString())
        }

        // This can't be null anyway
        private fun getHiddenApps(context: Context): MutableSet<String> {
            return HashSet(Utilities.getOmegaPrefs(context).hiddenPredictionAppSet)
        }

        private fun setHiddenApps(context: Context, hiddenApps: Set<String>) {
            Utilities.getOmegaPrefs(context).hiddenPredictionAppSet = hiddenApps
        }
    }
}