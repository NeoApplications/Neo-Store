@file:Suppress("PackageDirectoryMismatch")

package com.machiav3lli.fdroid.utils.extension.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

fun JsonParser.illegal(): Nothing {
    throw JsonParseException(this, "Illegal state")
}

interface KeyToken {
    val key: String
    val token: JsonToken

    fun number(key: String): Boolean = this.key == key && this.token.isNumeric
    fun string(key: String): Boolean = this.key == key && this.token == JsonToken.VALUE_STRING
    fun boolean(key: String): Boolean = this.key == key && this.token.isBoolean
    fun dictionary(key: String): Boolean = this.key == key && this.token == JsonToken.START_OBJECT
    fun array(key: String): Boolean = this.key == key && this.token == JsonToken.START_ARRAY
}

inline fun JsonParser.forEachKey(callback: JsonParser.(KeyToken) -> Unit) {
    var passKey = ""
    var passToken = JsonToken.NOT_AVAILABLE
    val keyToken = object : KeyToken {
        override val key: String
            get() = passKey
        override val token: JsonToken
            get() = passToken
    }
    while (true) {
        val token = nextToken()
        if (token == JsonToken.FIELD_NAME) {
            passKey = currentName()
            passToken = nextToken()
            callback(keyToken)
        } else if (token == JsonToken.END_OBJECT) {
            break
        } else {
            illegal()
        }
    }
}

inline fun JsonParser.forEach(requiredToken: JsonToken, callback: JsonParser.() -> Unit) {
    while (true) {
        val token = nextToken()
        when {
            token.isStructEnd      -> break
            token == requiredToken -> callback()
            token.isStructStart    -> skipChildren()
        }
    }
}

inline fun <T> JsonParser.collectNotNull(
    requiredToken: JsonToken,
    callback: JsonParser.() -> T?,
): List<T> {
    val list = mutableListOf<T>()
    forEach(requiredToken) {
        val result = callback()
        if (result != null) {
            list += result
        }
    }
    return list
}

fun JsonParser.collectNotNullStrings(): List<String> {
    return collectNotNull(JsonToken.VALUE_STRING) { text }
}

fun JsonParser.collectDistinctNotEmptyStrings(): List<String> {
    return collectNotNullStrings().filter { it.isNotEmpty() }.distinct().toList()
}