package com.looker.droidify.ui.compose.pages.app_detail.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.looker.droidify.R
import com.looker.droidify.entity.PermissionsType
import com.looker.droidify.utility.getLabels

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
            .clickable {
                onClick(
                    permissionsType.group?.name,
                    permissionsType.permissions.map { it.name })
            }
            .padding(vertical = 12.dp),
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
