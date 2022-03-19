package com.saggitt.omega

import android.content.Context
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Utilities
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.preferences.OmegaPreferences

class DeviceProfileOverrides(context: Context) {
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }

    fun getOverrides(defaultGrid: InvariantDeviceProfile.GridOption) = Options(prefs, defaultGrid)

    data class Options(
        var numHotseatColumns: Int,
        var numRows: Int,
        var numColumns: Int,
        var numAllAppsColumns: Int,
        var numFolderRows: Int,
        var numFolderColumns: Int,

        var iconSizeFactor: Float,
        var enableIconText: Boolean,
        var iconTextSizeFactor: Float,

        var allAppsIconSizeFactor: Float,
        var enableAllAppsIconText: Boolean,
        var allAppsIconTextSizeFactor: Float,

        val dbFile: String = "launcher_${numRows}_${numColumns}_${numHotseatColumns}.db"
    ) {

        constructor(
            prefs: OmegaPreferences,
            defaultGrid: InvariantDeviceProfile.GridOption,
        ) : this(
            numHotseatColumns = defaultGrid.numColumns,
            numRows = prefs.workspaceRows.get(defaultGrid),
            numColumns = prefs.workspaceColumns.get(defaultGrid),
            numAllAppsColumns = prefs.numAllAppsColumns.get(defaultGrid),
            numFolderRows = prefs.folderRows.toInt(),
            numFolderColumns = prefs.folderColumns.toInt(),

            iconSizeFactor = prefs.desktopIconScale,
            enableIconText = !prefs.hideAppLabels,
            iconTextSizeFactor = prefs.desktopTextScale,

            allAppsIconSizeFactor = prefs.allAppsIconScale,
            enableAllAppsIconText = !prefs.hideAllAppsAppLabels,
            allAppsIconTextSizeFactor = prefs.allAppsTextScale
        )

        fun apply(idp: InvariantDeviceProfile) {
            // apply grid size
            idp.numShownHotseatIcons = numHotseatColumns
            idp.numDatabaseHotseatIcons = numHotseatColumns
            idp.numRows = numRows
            idp.numColumns = numColumns
            idp.numAllAppsColumns = numAllAppsColumns
            idp.numFolderRows = numFolderRows
            idp.numFolderColumns = numFolderColumns

            // apply icon and text size
            idp.iconSize *= iconSizeFactor
            idp.iconTextSize *= (if (enableIconText) iconTextSizeFactor else 0f)
            idp.allAppsIconSize *= allAppsIconSizeFactor
            idp.allAppsIconTextSize *= (if (enableAllAppsIconText) allAppsIconTextSizeFactor else 0f)

            idp.dbFile = dbFile
        }
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::DeviceProfileOverrides)
    }
}
