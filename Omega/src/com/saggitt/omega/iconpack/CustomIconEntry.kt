package com.saggitt.omega.iconpack

import android.text.TextUtils
import com.saggitt.omega.util.asNonEmpty

data class CustomIconEntry(
    val packPackageName: String,
    val icon: String = "",
    val arg: String? = null
) {
    fun toPackString(): String {
        return packPackageName
    }

    override fun toString(): String = "$packPackageName|${icon}|${arg ?: ""}"

    companion object {
        fun fromString(string: String): CustomIconEntry = fromNullableString(string)!!

        fun fromNullableString(string: String?): CustomIconEntry? {
            if (string == null) return null
            if (string.contains("|")) {
                val parts = string.split("|")
                if (parts[0].contains("/")) return parseLegacy(string)
                if (parts.size == 1) {
                    return CustomIconEntry(parts[0])
                }
                return CustomIconEntry(parts[0], parts[1], parts[2].asNonEmpty())
            }
            return parseLegacy(string)
        }

        private fun parseLegacy(string: String): CustomIconEntry {
            val parts = string.split("/")
            val icon = TextUtils.join("/", parts.subList(1, parts.size))
            if (parts[0] == "omegaUriPack" && !icon.isNullOrBlank()) {
                val iconParts = icon.split("|")
                return CustomIconEntry(
                    parts[0],
                    iconParts[0],
                    iconParts[1].asNonEmpty()
                )
            }
            return CustomIconEntry(parts[0], icon)
        }
    }
}