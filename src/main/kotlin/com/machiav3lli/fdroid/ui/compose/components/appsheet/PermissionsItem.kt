package com.machiav3lli.fdroid.ui.compose.components.appsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.entity.PermissionsType
import com.machiav3lli.fdroid.utility.getLabels

@Composable
fun PermissionsItem(
    modifier: Modifier = Modifier,
    permissionsType: PermissionsType,
    onClick: (String?, List<String>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val pm = context.packageManager

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable {
                onClick(
                    permissionsType.group?.name,
                    permissionsType.permissions.map { it.name })
            }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = rememberAsyncImagePainter(
                model = if (permissionsType.group != null && permissionsType.group.icon != 0) {
                    permissionsType.group.loadUnbadgedIcon(pm)
                } else {
                    null
                } ?: (R.drawable.ic_perm_device_information)
            ),
            contentDescription = stringResource(
                id = permissionsType.group?.descriptionRes ?: R.string.unknown
            )
        )

        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = permissionsType.permissions.getLabels(context)
                    .joinToString(separator = "\n"),
                softWrap = true,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
