package com.machiav3lli.fdroid.utility

import com.machiav3lli.fdroid.HIGH_RISK_PERMISSIONS
import com.machiav3lli.fdroid.LOW_RISK_PERMISSIONS
import com.machiav3lli.fdroid.MEDIUM_RISK_PERMISSIONS
import com.machiav3lli.fdroid.NON_FREE_COUNTRIES_TRACKERS
import com.machiav3lli.fdroid.WIDESPREAD_TRACKERS
import com.machiav3lli.fdroid.entity.AntiFeature
import com.machiav3lli.fdroid.entity.PrivacyData
import com.machiav3lli.fdroid.entity.PrivacyNote
import com.machiav3lli.fdroid.entity.SourceType

fun PrivacyData.toPrivacyNote(): PrivacyNote {
    val permissionsNote = 100 - permissions.values.flatten().sumOf {
        when (it.name) {
            in HIGH_RISK_PERMISSIONS -> 15
            in MEDIUM_RISK_PERMISSIONS -> 7
            in LOW_RISK_PERMISSIONS -> 3
            else -> 2
        }.toInt()
    }.coerceAtMost(100)
    val trackersNote = 100 - (trackers.sumOf {
        it.categories.sumOf(String::trackerCategoryNote) * it.key.trackerNoteMultiplicator
    }).coerceAtMost(100)
    val sourceType = SourceType(
        open = AntiFeature.NO_SOURCE_SINCE !in antiFeatures,
        free = !antiFeatures.any {
            it == AntiFeature.NON_FREE_NET ||
                    it == AntiFeature.NON_FREE_UPSTREAM
        },
        independent = !antiFeatures.any {
            it == AntiFeature.NON_FREE_DEP || it == AntiFeature.NON_FREE_ASSETS
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
        "Analytics", "Identification" -> 10
        "Crash reporting" -> 5
        else -> 1
    }

private val Int.trackerNoteMultiplicator: Int
    get() = when (this) {
        in WIDESPREAD_TRACKERS,
        in NON_FREE_COUNTRIES_TRACKERS -> 2
        else -> 1
    }
