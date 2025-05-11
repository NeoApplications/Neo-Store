package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.ui.components.InfoChip
import com.machiav3lli.fdroid.utils.extension.text.formatSize
import com.machiav3lli.fdroid.utils.getAndroidVersionName
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.text.DateFormat
import java.util.Date

@Composable
fun AppInfoChips(
    list: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.height(54.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(items = list, key = { it }) { text ->
            InfoChip(
                text = text,
            )
        }
    }
}

@Composable
fun EmbeddedProduct.appInfoChips(installed: Installed?, latestRelease: Release?) = listOfNotNull(
    // TODO remove v when already there
    if (this.canUpdate(installed) && installed != null)
        "v${installed.version} → v$version"
    else if (installed != null) "v${installed.version}"
    else "v$version",
    displayRelease?.size?.formatSize().orEmpty(),
    DateFormat.getDateInstance().format(Date(product.updated)),
    *product.categories.toTypedArray(),
    when {
        Preferences[Preferences.Key.AndroidInsteadOfSDK] && latestRelease != null && latestRelease.minSdkVersion != 0 ->
            "${stringResource(id = R.string.min_android)} ${getAndroidVersionName(latestRelease.minSdkVersion)}"

        latestRelease?.minSdkVersion != 0                                                                             ->
            "${stringResource(id = R.string.min_sdk)} ${latestRelease?.minSdkVersion}"

        else                                                                                                          -> null
    },
    when {
        Preferences[Preferences.Key.AndroidInsteadOfSDK] && latestRelease != null && latestRelease.targetSdkVersion != 0 ->
            "${stringResource(id = R.string.target_android)} ${
                getAndroidVersionName(
                    latestRelease.targetSdkVersion
                )
            }"

        latestRelease?.targetSdkVersion != 0                                                                             ->
            "${stringResource(id = R.string.target_sdk)} ${latestRelease?.targetSdkVersion}"

        else                                                                                                             -> null
    },
    if (product.antiFeatures.isNotEmpty()) stringResource(id = R.string.anti_features)
    else null,
    *product.licenses.toTypedArray(),
).toImmutableList()