package com.machiav3lli.fdroid.database.entity

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.Score
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.LongProperty
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.app.AppSearchSchema
import com.machiav3lli.fdroid.entity.Author
import com.machiav3lli.fdroid.entity.Donate
import com.machiav3lli.fdroid.entity.Screenshot

@Document
data class ProductAS(
    @Namespace
    var namespace: String = "",
    @Id
    var id: String,
    @Score
    var score: Int = 0,

    var repositoryId: Long = 0L,

    @StringProperty(
        indexingType = AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES,
    )
    var packageName: String = "",
    @StringProperty(
        indexingType = AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES,
    )
    var label: String = "",
    @StringProperty(
        indexingType = AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES,
    )
    var summary: String = "",
    @StringProperty(
        indexingType = AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES,
    )
    var description: String = "",

    @LongProperty
    var added: Long = 0L,
    @LongProperty
    var updated: Long = 0L,

    var icon: String = "",
    var metadataIcon: String = "",
    var releases: List<Release> = emptyList(),
    var categories: List<String> = emptyList(),
    var antiFeatures: List<String> = emptyList(),
    var licenses: List<String> = emptyList(),
    var donates: List<Donate> = emptyList(),
    var screenshots: List<Screenshot> = emptyList(),
    var versionCode: Long = 0L,
    var suggestedVersionCode: Long = 0L,
    var signatures: List<String> = emptyList(),
    var compatible: Boolean = false,
    var author: Author = Author(),
) {
}