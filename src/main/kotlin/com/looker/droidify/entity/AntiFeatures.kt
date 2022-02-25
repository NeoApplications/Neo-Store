package com.looker.droidify.entity

import com.looker.droidify.R

sealed class AntiFeatures(val name: String, val string: Int) {
    object Ads : AntiFeatures("Ads", R.string.has_advertising)
    object ApplicationDebuggable :
        AntiFeatures("ApplicationDebuggable", R.string.compiled_for_debugging)

    object DisabledAlgorithm :
        AntiFeatures("DisabledAlgorithm", R.string.signed_using_unsafe_algorithm)

    object KnownVuln : AntiFeatures("KnownVuln", R.string.has_security_vulnerabilities)
    object NoSourceSince : AntiFeatures("NoSourceSince", R.string.source_code_no_longer_available)
    object NonFreeAdd : AntiFeatures("NonFreeAdd", R.string.promotes_non_free_software)
    object NonFreeAssets : AntiFeatures("NonFreeAssets", R.string.contains_non_free_media)
    object NonFreeDep : AntiFeatures("NonFreeDep", R.string.has_non_free_dependencies)
    object NonFreeNet : AntiFeatures("NonFreeNet", R.string.promotes_non_free_network_services)
    object Tracking : AntiFeatures("Tracking", R.string.tracks_or_reports_your_activity)
    object UpstreamNonFree :
        AntiFeatures("UpstreamNonFree", R.string.upstream_source_code_is_not_free)

    object Others : AntiFeatures("Others", R.string.unknown_FORMAT)
}

fun String.toAntiFeatures() = when (this) {
    AntiFeatures.Ads.name -> AntiFeatures.Ads
    AntiFeatures.ApplicationDebuggable.name -> AntiFeatures.ApplicationDebuggable
    AntiFeatures.DisabledAlgorithm.name -> AntiFeatures.Ads
    AntiFeatures.KnownVuln.name -> AntiFeatures.Ads
    AntiFeatures.NoSourceSince.name -> AntiFeatures.Ads
    AntiFeatures.NonFreeAdd.name -> AntiFeatures.Ads
    AntiFeatures.NonFreeAssets.name -> AntiFeatures.Ads
    AntiFeatures.NonFreeDep.name -> AntiFeatures.Ads
    AntiFeatures.NonFreeNet.name -> AntiFeatures.Ads
    AntiFeatures.Tracking.name -> AntiFeatures.Ads
    AntiFeatures.UpstreamNonFree.name -> AntiFeatures.Ads
    else -> AntiFeatures.Others
}