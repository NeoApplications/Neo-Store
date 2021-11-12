package com.saggitt.omega.iconpack

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.icons.CustomAdaptiveIconDrawable
import com.saggitt.omega.util.Config

class IconPackProvider(private val context: Context) {
    private val iconPacks = mutableMapOf<String, IconPack?>()

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

        val systemIcon = CustomAdaptiveIconDrawable.wrapNonNull(
            ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!
        )
        val defaultIconPack =
            IconPackInfo(context.getString(R.string.icon_pack_default), "", systemIcon)

        return listOf(defaultIconPack) + iconPacks.sortedBy { it.name }
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