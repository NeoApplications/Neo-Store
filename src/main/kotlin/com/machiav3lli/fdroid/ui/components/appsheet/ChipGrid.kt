package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.ui.components.SelectChip
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoChips(
    modifier: Modifier = Modifier,
    product: Product,
    latestRelease: Release?,
    installed: Installed?,
) {
    val list = listOfNotNull(
        if (product.canUpdate(installed) && installed != null)
            "v${installed.version} → v${product.version}"
        else if (installed != null) "v${installed.version}"
        else "v${product.version}",
        product.displayRelease?.size?.formatSize().orEmpty(),
        DateFormat.getDateInstance().format(Date(product.updated)),
        if (latestRelease?.minSdkVersion != 0) "${stringResource(id = R.string.min_sdk)} ${latestRelease?.minSdkVersion}"
        else null,
        if (latestRelease?.targetSdkVersion != 0) "${stringResource(id = R.string.target_sdk)} ${latestRelease?.targetSdkVersion}"
        else null,
        if (product.antiFeatures.isNotEmpty()) stringResource(id = R.string.anti_features)
        else null,
        *product.licenses.toTypedArray(),
    )

    LazyRow(
        modifier = modifier.height(54.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(list) { text ->
            SelectChip(
                text = text,
                checked = false
            )
        }
    }
}