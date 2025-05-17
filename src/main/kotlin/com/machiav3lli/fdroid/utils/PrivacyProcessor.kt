package com.machiav3lli.fdroid.utils

import android.content.pm.PermissionInfo
import com.machiav3lli.fdroid.HIGH_RISK_PERMISSIONS
import com.machiav3lli.fdroid.LOW_RISK_PERMISSIONS
import com.machiav3lli.fdroid.MEDIUM_RISK_PERMISSIONS
import com.machiav3lli.fdroid.NON_FREE_COUNTRIES_TRACKERS
import com.machiav3lli.fdroid.WIDESPREAD_TRACKERS
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.Tracker
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.PermissionGroup
import com.machiav3lli.fdroid.data.entity.PrivacyData
import com.machiav3lli.fdroid.data.entity.PrivacyNote
import com.machiav3lli.fdroid.data.entity.SourceType

fun PrivacyData.toPrivacyNote(): PrivacyNote {
    val permissionsNote = 100 - permissions.privacyPoints.coerceAtMost(100)
    val trackersNote = 100 - trackers.privacyPoints.coerceAtMost(100)
    val sourceType = SourceType(
        open = !antiFeatures.map(AntiFeatureDetails::name)
            .contains(AntiFeature.NO_SOURCE_SINCE.key),
        free = !antiFeatures.any {
            it.name == AntiFeature.NON_FREE_NET.key ||
                    it.name == AntiFeature.NON_FREE_UPSTREAM.key
        },
        independent = !antiFeatures.any {
            it.name == AntiFeature.NON_FREE_DEP.key ||
                    it.name == AntiFeature.NON_FREE_ASSETS.key
        }
    )
    return PrivacyNote(
        permissionsNote,
        trackersNote,
        sourceType
    )
}

private val String.trackerCategoryNote: Int
    get() = when (this) {
        "Ads", "Profiling", "Location" -> 20
        "Analytics", "Identification"  -> 10
        "Crash reporting"              -> 5
        else                           -> 1
    }

private val Int.trackerNoteMultiplicator: Int
    get() = when (this) {
        in WIDESPREAD_TRACKERS,
        in NON_FREE_COUNTRIES_TRACKERS,
             -> 2

        else -> 1
    }

val Map<PermissionGroup, List<PermissionInfo>>.privacyPoints
    get() = values.flatten().sumOf {
        when (it.name) {
            in HIGH_RISK_PERMISSIONS   -> 15
            in MEDIUM_RISK_PERMISSIONS -> 7
            in LOW_RISK_PERMISSIONS    -> 3
            else                       -> 2
        }.toInt()
    }

val List<Tracker>.privacyPoints
    get() = sumOf {
        it.categories.sumOf(String::trackerCategoryNote) * it.key.trackerNoteMultiplicator
    }