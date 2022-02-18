package com.looker.droidify.database

class QueryBuilder {
    companion object {
        fun trimQuery(query: String): String {
            return query.lines().map { it.trim() }.filter { it.isNotEmpty() }
                .joinToString(separator = " ")
        }
    }

    private val builder = StringBuilder()
    val arguments = mutableListOf<String>()

    operator fun plusAssign(query: String) {
        if (builder.isNotEmpty()) {
            builder.append(" ")
        }
        builder.append(trimQuery(query))
    }

    operator fun remAssign(argument: String) {
        this.arguments += argument
    }

    operator fun remAssign(arguments: List<String>) {
        this.arguments += arguments
    }

    fun build() = builder.toString()
}