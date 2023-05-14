package com.machiav3lli.fdroid.database

class QueryBuilder {

    var initWhere: Boolean = false

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

    fun addFrom(from: String): QueryBuilder {
        this += "FROM $from"
        return this
    }

    fun addWhere(condition: String): QueryBuilder {
        this += (if (initWhere) "AND " else "WHERE ") + condition
        initWhere = true
        return this
    }

    fun addJoin(table: String, left: Boolean, condition: String): QueryBuilder {
        this += "${if (left) "LEFT JOIN" else "INNER JOIN"} $table ON $condition"
        return this
    }

    fun addOrderBy(condition: String): QueryBuilder {
        this += "ORDER BY $condition"
        return this
    }

    fun addGroupBy(condition: String): QueryBuilder {
        this += "GROUP BY $condition"
        return this
    }

    fun addArgument(argument: String): QueryBuilder {
        this %= argument
        return this
    }

    fun build() = builder.toString()
}