package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Permission
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X

@Composable
fun PermissionItem(
    item: Permission,
    onClick: () -> Unit = {},
    onIgnore: () -> Unit = {},
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        headlineContent = {
            Row(modifier = Modifier.wrapContentHeight()) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = stringResource(id = item.nameId),
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = stringResource(id = item.nameId),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        supportingContent = {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = stringResource(id = item.descriptionId),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
                if (item.warningTextId != -1) {
                    Text(
                        text = stringResource(id = item.warningTextId),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
                if (item.ignorePref != null) {
                    ActionButton(
                        text = stringResource(id = R.string.ignore),
                        icon = Phosphor.X,
                        positive = false,
                        onClick = {
                            Preferences[item.ignorePref] = true
                            onIgnore()
                        }
                    )
                }
            }
        }
    )
}