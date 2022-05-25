package com.saggitt.omega.iconpack

import android.content.Context
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
    private val systemIconPack = SystemIconPack(context)
    val systemIcon = CustomAdaptiveIconDrawable.wrapNonNull(
        ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!
    )

    fun getIconPackOrSystem(packageName: String): IconPack? {
        if (packageName == "") return systemIconPack
        return getIconPack(packageName)
    }

    private fun getIconPack(packageName: String): IconPack? {
        return iconPacks.getOrPut(packageName) {
            try {
                CustomIconPack(context, packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
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

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconPackProvider)
    }
}

data class IconPackInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)