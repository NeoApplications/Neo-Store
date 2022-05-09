package com.saggitt.omega.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.saggitt.omega.groups.AppGroupsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizationOption(
    modifier: Modifier = Modifier,
    type: AppGroupsManager.CategorizationType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 2.dp,
        color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(
                        id = when (type) {
                            AppGroupsManager.CategorizationType.Folders -> R.string.app_categorization_folders
                            else -> R.string.app_categorization_tabs
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(
                        id = when (type) {
                            AppGroupsManager.CategorizationType.Folders -> R.string.pref_appcategorization_folders_summary
                            else -> R.string.pref_appcategorization_tabs_summary
                        }
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Column(
                    modifier = Modifier
                        .width(48.dp)
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}