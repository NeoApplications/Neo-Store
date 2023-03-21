package com.machiav3lli.fdroid.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Permission
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionItem(
    item: Permission,
    onClick: () -> Unit = {},
    onIgnore: () -> Unit = {},
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
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
}