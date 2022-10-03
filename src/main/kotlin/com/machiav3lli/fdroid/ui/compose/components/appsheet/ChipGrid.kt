package com.machiav3lli.fdroid.ui.compose.components.appsheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.ui.compose.components.CategoryChip
import com.machiav3lli.fdroid.ui.compose.utils.CustomChip
import com.machiav3lli.fdroid.ui.compose.utils.StaggeredGrid
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import java.text.DateFormat
import java.util.*

@Composable
fun AppInfoChips(
    modifier: Modifier = Modifier,
    product: Product,
    latestRelease: Release?
) {
    val list = listOfNotNull(
        "v${product.version}",
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
            CategoryChip(
                category = text,
                isSelected = false
            )
        }
    }
}

// TODO: Convert Permissions and AntiFeatures to Custom Interface

@Composable
fun PermissionGrid(
    modifier: Modifier = Modifier,
    permissions: List<String>
) {
    StaggeredGrid(modifier = modifier.horizontalScroll(rememberScrollState())) {
        permissions.forEach {
            CustomChip(modifier = Modifier.padding(horizontal = 2.dp), text = it)
        }
    }
}

@Composable
fun AntiFeaturesGrid(
    modifier: Modifier = Modifier,
    antiFeatures: List<String>
) {
    StaggeredGrid(modifier = modifier.horizontalScroll(rememberScrollState())) {
        antiFeatures.forEach {
            CustomChip(
                modifier = Modifier.padding(horizontal = 2.dp),
                text = it,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                borderColor = MaterialTheme.colorScheme.error
            )
        }
    }
}