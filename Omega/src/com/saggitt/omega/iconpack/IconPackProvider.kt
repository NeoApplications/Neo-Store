package com.saggitt.omega.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.android.launcher3.icons.ClockDrawableWrapper
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.icons.ClockMetadata
import com.saggitt.omega.icons.CustomAdaptiveIconDrawable
import com.saggitt.omega.util.Config

class IconPackProvider(private val context: Context) {
    private val iconPacks = mutableMapOf<String, IconPack?>()
    val systemIcon = CustomAdaptiveIconDrawable.wrapNonNull(
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!
    )

    fun getIconPack(packageName: String): IconPack? {
        if (packageName == "") {
            return null
        }
        return iconPacks.getOrPut(packageName) {
            try {
                val packResources = context.packageManager.getResourcesForApplication(packageName)
                IconPack(context, packageName, packResources)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    private fun getIconPackInternal(
        name: String,
        put: Boolean = true,
        load: Boolean = false
    ): IconPack? {
        //if (name == defaultPack.packPackageName) return defaultPack
        //if (name == uriPack.packPackageName) return uriPack
        return if (isPackProvider(context, name)) {
            iconPacks[name]?.apply { if (load) loadBlocking() }
        } else null
    }

    fun getIconPack(name: String, put: Boolean = true, load: Boolean = false): IconPack? {
        return getIconPackInternal(name, put, load)
    }

    fun getIconPackList(): List<IconPackInfo> {
        val pm = context.packageManager

        val iconPacks = Config.ICON_INTENTS
            .flatMap { pm.queryIntentActivities(it, 0) }
            .associateBy { it.activityInfo.packageName }
            .mapTo(mutableSetOf()) { (_, info) ->
                IconPackInfo(
                    info.loadLabel(pm).toString(),
                    info.activityInfo.packageName,
                    CustomAdaptiveIconDrawable.wrapNonNull(info.loadIcon(pm))
                )
            }

        val defaultIconPack =
            IconPackInfo(context.getString(R.string.icon_pack_default), "", systemIcon)

        return listOf(defaultIconPack) + iconPacks.sortedBy { it.name }
    }

    fun getClockMetadata(iconEntry: IconEntry): ClockMetadata? {
        val iconPack = getIconPackOrSystem(iconEntry.packPackageName) ?: return null
        return iconPack.getClock(iconEntry)
    }

    fun getDrawable(iconEntry: IconEntry, iconDpi: Int, user: UserHandle): Drawable? {
        val iconPack = getIconPackOrSystem(iconEntry.packPackageName) ?: return null
        iconPack.loadBlocking()
        val drawable = iconPack.getIcon(iconEntry, iconDpi) ?: return null
        val clockMetadata =
            if (user == Process.myUserHandle()) iconPack.getClock(iconEntry) else null
        if (clockMetadata != null) {
            val clockDrawable = ClockDrawableWrapper.forMeta(Build.VERSION.SDK_INT, clockMetadata) {
                drawable
            }
            if (clockDrawable != null) {
                return clockDrawable
            }
        }
        return drawable
    }

    private fun getIconPackOrSystem(packageName: String): IconPack? {
        if (packageName == "") return null
        return getIconPack(packageName)
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconPackProvider)

        fun getInstance(context: Context): IconPackProvider {
            if (INSTANCE == null) {
                INSTANCE[context] = IconPackProvider(context.applicationContext)
            }
            return INSTANCE[context]!!
        }

        internal fun isPackProvider(context: Context, packageName: String?): Boolean {
            if (packageName != null && packageName.isNotEmpty()) {
                return Config.ICON_INTENTS.firstOrNull {
                    context.packageManager.queryIntentActivities(
                        Intent(it).setPackage(packageName), PackageManager.GET_META_DATA
                    ).iterator().hasNext()
                } != null
            }
            return false
        }
    }
}

data class IconPackInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)