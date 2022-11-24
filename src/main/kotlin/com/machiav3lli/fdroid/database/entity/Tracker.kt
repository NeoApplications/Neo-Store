package com.machiav3lli.fdroid.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tracker(
    @PrimaryKey
    val key: Int = 0,
    override val name: String = String(),
    override val network_signature: String = String(),
    override val code_signature: String = String(),
    override val creation_date: String = String(),
    override val website: String = String(),
    override val description: String = String(),
    override val categories: List<String> = emptyList(),
    override val documentation: List<String> = emptyList(),
) : TrackerData(
    name,
    network_signature,
    code_signature,
    creation_date,
    website,
    description,
    categories,
    documentation,
)

open class TrackerData(
    open val name: String = String(),
    open val network_signature: String = String(),
    open val code_signature: String = String(),
    open val creation_date: String = String(),
    open val website: String = String(),
    open val description: String = String(),
    open val categories: List<String> = emptyList(),
    open val documentation: List<String> = emptyList(),
)

data class Trackers(
    val trackers: Map<String, TrackerData> = emptyMap()
)