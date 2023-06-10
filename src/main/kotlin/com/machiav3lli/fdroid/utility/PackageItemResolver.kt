package com.machiav3lli.fdroid.utility

import android.content.Context
import android.content.pm.PackageItemInfo
import android.content.pm.PermissionInfo
import android.content.res.Resources
import com.machiav3lli.fdroid.CALENDAR_PERMISSIONS
import com.machiav3lli.fdroid.CAMERA_PERMISSIONS
import com.machiav3lli.fdroid.CONTACTS_PERMISSIONS
import com.machiav3lli.fdroid.INTERNET_PERMISSIONS
import com.machiav3lli.fdroid.LOCATION_PERMISSIONS
import com.machiav3lli.fdroid.MICROPHONE_PERMISSIONS
import com.machiav3lli.fdroid.NEARBY_DEVICES_PERMISSIONS
import com.machiav3lli.fdroid.PHONE_PERMISSIONS
import com.machiav3lli.fdroid.SMS_PERMISSIONS
import com.machiav3lli.fdroid.STORAGE_PERMISSIONS
import com.machiav3lli.fdroid.entity.PermissionGroup
import com.machiav3lli.fdroid.entity.PermissionGroup.Companion.getPermissionGroup
import com.machiav3lli.fdroid.utility.extension.android.Android
import java.util.*

object PackageItemResolver {
    class LocalCache {
        internal val resources = mutableMapOf<String, Resources>()
    }

    private data class CacheKey(val locales: List<Locale>, val packageName: String, val resId: Int)

    private val cache = mutableMapOf<CacheKey, String?>()

    private fun load(
        context: Context, localCache: LocalCache, packageName: String,
        nonLocalized: CharSequence?, resId: Int,
    ): CharSequence? {
        return when {
            nonLocalized != null -> {
                nonLocalized
            }

            resId != 0           -> {
                val localesList = context.resources.configuration.locales
                val locales = (0 until localesList.size()).map(localesList::get)
                val cacheKey = CacheKey(locales, packageName, resId)
                if (cache.containsKey(cacheKey)) {
                    cache[cacheKey]
                } else {
                    val resources = localCache.resources[packageName] ?: run {
                        val resources = try {
                            val resources =
                                context.packageManager.getResourcesForApplication(packageName)
                            @Suppress("DEPRECATION")
                            resources.updateConfiguration(context.resources.configuration, null)
                            resources
                        } catch (e: Exception) {
                            null
                        }
                        resources?.let { localCache.resources[packageName] = it }
                        resources
                    }
                    val label = resources?.getString(resId)
                    cache[cacheKey] = label
                    label
                }
            }

            else                 -> {
                null
            }
        }
    }

    fun loadLabel(
        context: Context,
        localCache: LocalCache,
        packageItemInfo: PackageItemInfo,
    ): CharSequence? {
        return load(
            context, localCache, packageItemInfo.packageName,
            packageItemInfo.nonLocalizedLabel, packageItemInfo.labelRes
        )
    }

    fun loadDescription(
        context: Context,
        localCache: LocalCache,
        permissionInfo: PermissionInfo,
    ): CharSequence? {
        return load(
            context, localCache, permissionInfo.packageName,
            permissionInfo.nonLocalizedDescription, permissionInfo.descriptionRes
        )
    }

    fun getPermissionGroup(permissionInfo: PermissionInfo): PermissionGroup {
        return if (Android.sdk(29)) {
            when (permissionInfo.name) {
                in CONTACTS_PERMISSIONS       -> PermissionGroup.Contacts
                in CALENDAR_PERMISSIONS       -> PermissionGroup.Calendar
                in SMS_PERMISSIONS            -> PermissionGroup.SMS
                in STORAGE_PERMISSIONS        -> PermissionGroup.Storage
                in PHONE_PERMISSIONS          -> PermissionGroup.Phone
                in LOCATION_PERMISSIONS       -> PermissionGroup.Location
                in MICROPHONE_PERMISSIONS     -> PermissionGroup.Microphone
                in CAMERA_PERMISSIONS         -> PermissionGroup.Camera
                in NEARBY_DEVICES_PERMISSIONS -> PermissionGroup.NearbyDevices
                in INTERNET_PERMISSIONS       -> PermissionGroup.Internet
                else                          -> PermissionGroup.Other
            }
        } else {
            permissionInfo.group?.getPermissionGroup() ?: PermissionGroup.Other
        }
    }
}
