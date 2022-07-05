package com.machiav3lli.fdroid.utility

sealed interface PreferenceType {
    data class Switch(val title: String, val description: String, val key: String) : PreferenceType

    data class Slider(
        val title: String,
        val value: Float,
        val range: ClosedFloatingPointRange<Float>,
        val key: String
    ) : PreferenceType

    data class Data(val title: String, val description: String, val key: String) : PreferenceType
}