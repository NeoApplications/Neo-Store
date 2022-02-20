package com.looker.droidify.entity

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.looker.droidify.utility.extension.json.forEachKey

data class Author(val name: String, val email: String, val web: String)

sealed class Donate {
    data class Regular(val url: String) : Donate()
    data class Bitcoin(val address: String) : Donate()
    data class Litecoin(val address: String) : Donate()
    data class Flattr(val id: String) : Donate()
    data class Liberapay(val id: String) : Donate()
    data class OpenCollective(val id: String) : Donate()

    fun serialize(generator: JsonGenerator) {
        when (this) {
            is Regular -> {
                generator.writeStringField("type", "")
                generator.writeStringField("url", url)
            }
            is Bitcoin -> {
                generator.writeStringField("type", "bitcoin")
                generator.writeStringField("address", address)
            }
            is Litecoin -> {
                generator.writeStringField("type", "litecoin")
                generator.writeStringField("address", address)
            }
            is Flattr -> {
                generator.writeStringField("type", "flattr")
                generator.writeStringField("id", id)
            }
            is Liberapay -> {
                generator.writeStringField("type", "liberapay")
                generator.writeStringField("id", id)
            }
            is OpenCollective -> {
                generator.writeStringField("type", "openCollective")
                generator.writeStringField("id", id)
            }
        }::class
    }

    companion object {
        fun deserialize(parser: JsonParser): Donate? {
            var type = ""
            var url = ""
            var address = ""
            var id = ""
            parser.forEachKey {
                when {
                    it.string("type") -> type = valueAsString
                    it.string("url") -> url = valueAsString
                    it.string("address") -> address = valueAsString
                    it.string("id") -> id = valueAsString
                    else -> skipChildren()
                }
            }
            return when (type) {
                "" -> Regular(url)
                "bitcoin" -> Bitcoin(address)
                "litecoin" -> Litecoin(address)
                "flattr" -> Flattr(id)
                "liberapay" -> Liberapay(id)
                "openCollective" -> OpenCollective(id)
                else -> null
            }
        }
    }
}

class Screenshot(val locale: String, val type: Type, val path: String) {
    enum class Type(val jsonName: String) {
        PHONE("phone"),
        SMALL_TABLET("smallTablet"),
        LARGE_TABLET("largeTablet")
    }

    val identifier: String
        get() = "$locale.${type.name}.$path"

    fun serialize(generator: JsonGenerator) {
        generator.writeStringField("locale", locale)
        generator.writeStringField("type", type.jsonName)
        generator.writeStringField("path", path)
    }

    companion object {
        fun deserialize(parser: JsonParser): Screenshot? {
            var locale = ""
            var type = ""
            var path = ""
            parser.forEachKey {
                when {
                    it.string("locale") -> locale = valueAsString
                    it.string("type") -> type = valueAsString
                    it.string("path") -> path = valueAsString
                    else -> skipChildren()
                }
            }
            return Type.values().find { it.jsonName == type }?.let { Screenshot(locale, it, path) }
        }
    }
}