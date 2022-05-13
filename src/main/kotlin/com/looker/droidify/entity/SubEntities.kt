package com.looker.droidify.entity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.looker.droidify.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Author(val name: String = "", val email: String = "", val web: String = "") {
    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Author>(json)
    }
}

@Serializable
sealed class Donate {
    @Serializable
    data class Regular(val url: String) : Donate()

    @Serializable
    data class Bitcoin(val address: String) : Donate()

    @Serializable
    data class Litecoin(val address: String) : Donate()

    @Serializable
    data class Flattr(val id: String) : Donate()

    @Serializable
    data class Liberapay(val id: String) : Donate()

    @Serializable
    data class OpenCollective(val id: String) : Donate()

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Donate>(json)
    }
}

@Serializable
class Screenshot(val locale: String, val type: Type, val path: String) {
    enum class Type(val jsonName: String) {
        PHONE("phone"),
        SMALL_TABLET("smallTablet"),
        LARGE_TABLET("largeTablet")
    }

    val identifier: String
        get() = "$locale.${type.name}.$path"

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Screenshot>(json)
    }
}

enum class Action(@StringRes val titleResId: Int, @DrawableRes val iconResId: Int) {
    INSTALL(R.string.install, R.drawable.ic_download),
    UPDATE(R.string.update, R.drawable.ic_download),
    LAUNCH(R.string.launch, R.drawable.ic_launch),
    DETAILS(R.string.details, R.drawable.ic_tune),
    UNINSTALL(R.string.uninstall, R.drawable.ic_delete),
    CANCEL(R.string.cancel, R.drawable.ic_cancel),
    SHARE(R.string.share, R.drawable.ic_share)
}