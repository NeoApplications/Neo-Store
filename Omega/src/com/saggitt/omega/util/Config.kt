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
package com.saggitt.omega.util

import android.app.KeyguardManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.res.Resources
import android.hardware.biometrics.BiometricManager.Authenticators
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.os.UserHandle
import android.text.TextUtils
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.launcher3.BuildConfig
import com.android.launcher3.LauncherModel
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.shortcuts.DeepShortcutManager
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.android.launcher3.util.Themes
import java.util.*

class Config(var mContext: Context) {
    fun defaultEnableBlur(): Boolean {
        return mContext.resources.getBoolean(R.bool.config_default_enable_blur)
    }

    val defaultSearchProvider: String
        get() = mContext.resources.getString(R.string.config_default_search_provider)
    val defaultIconPacks: Array<String>
        get() = mContext.resources.getStringArray(R.array.config_default_icon_packs)
    val defaultBlurStrength: Float
        get() {
            val typedValue = TypedValue()
            mContext.resources.getValue(R.dimen.config_default_blur_strength, typedValue, true)
            return typedValue.float
        }

    fun defaultFolderBackground(): Int {
        return Themes.getAttrColor(mContext, R.attr.folderFillColor)
    }

    fun setAppLanguage(androidLC: String) {
        val locale = getLocaleByAndroidCode(androidLC)
        val config = mContext.resources.configuration
        val mLocale =
            if (androidLC.isNotEmpty()) locale else Resources.getSystem().configuration.locales[0]
        config.setLocale(mLocale)
        mContext.createConfigurationContext(config)
    }

    fun getLocaleByAndroidCode(androidLC: String): Locale {
        return if (!TextUtils.isEmpty(androidLC)) {
            if (androidLC.contains("-r")) Locale(
                androidLC.substring(0, 2),
                androidLC.substring(4, 6)
            ) // de-rAt
            else Locale(androidLC) // de
        } else Resources.getSystem().configuration.locales[0]
    }

    val appVersionName: String
        get() = try {
            val manager = mContext.packageManager
            val info = manager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
            info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "?"
        }
    val appVersionCode: Int
        get() = try {
            val manager = mContext.packageManager
            val info = manager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode.toInt()
            } else {
                info.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0
        }

    fun getBuildConfigValue(fieldName: String, defaultValue: String): String {
        val field = getBuildConfigValue(fieldName)
        return if (field is String) {
            field
        } else defaultValue
    }

    fun getBuildConfigValue(fieldName: String): Any? {
        val pkg = "com.android.launcher3.BuildConfig"
        return try {
            val c = Class.forName(pkg)
            c.getField(fieldName)[null]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val installSource: String
        get() {
            var src = ""
            try {
                src = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ({
                    mContext.packageManager.getInstallSourceInfo(mContext.packageName).initiatingPackageName
                }).toString() else ({
                    mContext.packageManager.getInstallerPackageName(mContext.packageName)
                }).toString()
            } catch (ignored: IllegalArgumentException) {
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            return when {
                src.isEmpty() || src == "Function0" || src == "null" -> {
                    "Sideloaded"
                }
                "com.android.vending" == src || "com.google.android.feedback" == src -> {
                    "Google Play Store"
                }
                "org.fdroid.fdroid.privileged" == src || "org.fdroid.fdroid" == src -> {
                    "F-Droid"
                }
                "com.github.yeriomin.yalpstore" == src -> {
                    "Yalp Store"
                }
                "cm.aptoide.pt" == src -> {
                    "Aptoide"
                }
                src.contains(".amazon.",true) -> {
                    "Amazon Appstore"
                }
                else -> {
                    src
                }
            }
        }

    companion object {
        //APP DRAWER SORT MODE
        const val SORT_AZ = 0
        const val SORT_ZA = 1
        const val SORT_LAST_INSTALLED = 2
        const val SORT_MOST_USED = 3
        const val SORT_BY_COLOR = 4

        //APP DRAWER LAYOUT MODE
        const val DRAWER_VERTICAL = 0
        const val DRAWER_VERTICAL_LIST = 1
        const val DRAWER_PAGED = 2

        //PERMISION FLAGS
        const val REQUEST_PERMISSION_STORAGE_ACCESS = 666
        const val REQUEST_PERMISSION_LOCATION_ACCESS = 667
        const val CODE_EDIT_ICON = 100
        const val GOOGLE_QSB = "com.google.android.googlequicksearchbox"
        val ICON_INTENTS = arrayOf(
            "com.novalauncher.THEME",
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.fede.launcher.THEME_ICONPACK",
            "com.gau.go.launcherex.theme",
            "com.dlto.atom.launcher.THEME",
            "net.oneplus.launcher.icons.ACTION_PICK_ICON"
        )

        fun reloadIcon(
            shortcutManager: DeepShortcutManager,
            model: LauncherModel,
            user: UserHandle?,
            pkg: String?
        ) {
            model.onAppIconChanged(pkg, user)
            if (shortcutManager.wasLastCallSuccess()) {
                val shortcuts: List<ShortcutInfo> =
                    shortcutManager.queryForPinnedShortcuts(pkg, user)
                if (shortcuts.isNotEmpty()) {
                    model.updatePinnedShortcuts(pkg, shortcuts, user)
                }
            }
        }

        fun newInstance(s: String?, vararg array: String?): IntentFilter {
            val intentFilter = IntentFilter()
            val length = array.size
            var i = 0
            while (i < length) {
                intentFilter.addAction(array[i])
                ++i
            }
            intentFilter.addDataScheme("package")
            intentFilter.addDataSchemeSpecificPart(s, 0)
            return intentFilter
        }

        /**
         * Shows authentication screen to confirm credentials (pin, pattern or password) for the current
         * user of the device.
         *
         * @param context The {@code Context} used to get {@code KeyguardManager} service
         * @param title the {@code String} which will be shown as the pompt title
         * @param successRunnable The {@code Runnable} which will be executed if the user does not setup
         *                        device security or if lock screen is unlocked
         */
        @RequiresApi(Build.VERSION_CODES.R)
        fun showLockScreen(context: Context, title: String, successRunnable: Runnable) {
            if (hasSecureKeyguard(context)) {

                val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        successRunnable.run()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        //Do nothing
                    }
                }

                val bp = BiometricPrompt.Builder(context)
                    .setTitle(title)
                    .setAllowedAuthenticators(
                        Authenticators.BIOMETRIC_STRONG or
                                Authenticators.DEVICE_CREDENTIAL
                    )
                    .build()

                val handler = MAIN_EXECUTOR.handler
                bp.authenticate(
                    CancellationSignal(), { runnable: Runnable ->
                        handler.post(runnable)
                    },
                    authenticationCallback
                )
            } else {
                // Notify the user a secure keyguard is required for protected apps,
                // but allow to set hidden apps
                Toast.makeText(context, R.string.trust_apps_no_lock_error, Toast.LENGTH_LONG)
                    .show()
                successRunnable.run()
            }
        }

        private fun hasSecureKeyguard(context: Context): Boolean {
            val keyguardManager = context.getSystemService(
                KeyguardManager::class.java
            )
            return keyguardManager != null && keyguardManager.isKeyguardSecure
        }

        fun isAppProtected(context: Context, componentKey: ComponentKey): Boolean {
            var result = false
            val protectedApps = ArrayList(Utilities.getOmegaPrefs(context).protectedAppsSet
                .map { Utilities.makeComponentKey(context, it) })

            if (protectedApps.contains(componentKey)) {
                result = true
            }
            return result
        }
    }
}