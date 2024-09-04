package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.NonBooleanPrefsMeta
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.content.PrefsEntries
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.DialogNegativeButton
import com.machiav3lli.fdroid.ui.components.DialogPositiveButton
import com.machiav3lli.fdroid.ui.components.FlatActionButton
import com.machiav3lli.fdroid.ui.components.SingleSelectionListItem
import com.machiav3lli.fdroid.ui.compose.utils.blockShadow
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.Utils.getLocaleOfCode

@Composable
fun LanguagePrefDialogUI(
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefKey = Preferences.Key.Language
    var selected by remember { mutableStateOf(Preferences[prefKey]) }
    val entryPairs = Utils.languagesList
        .associateWith { Utils.translateLocale(context.getLocaleOfCode(it)) }.toList()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(NonBooleanPrefsMeta[prefKey] ?: -1),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                items(
                    items = entryPairs,
                    key = { it.first },
                ) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.first)
                    }
                    SingleSelectionListItem(
                        text = it.second,
                        isSelected = isSelected.value
                    ) {
                        selected = it.first
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DialogNegativeButton(
                    onClick = { openDialogCustom.value = false }
                )
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        Preferences[prefKey] = selected
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}


@Composable
fun EnumSelectionPrefDialogUI(
    prefKey: Preferences.Key<Preferences.Enumeration<*>>,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(Preferences[prefKey]) }
    val entryPairs = PrefsEntries[prefKey]?.entries?.toList() ?: emptyList()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(NonBooleanPrefsMeta[prefKey] ?: -1),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                items(
                    items = entryPairs,
                    key = { it.key.toString() },
                ) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.key)
                    }
                    SingleSelectionListItem(
                        text = stringResource(id = it.value),
                        isSelected = isSelected.value
                    ) {
                        selected = it.key
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                DialogNegativeButton(
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        Preferences[prefKey] = selected
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun <T> ActionSelectionDialogUI(
    titleId: Int,
    options: Map<T, String>,
    openDialogCustom: MutableState<Boolean>,
    onAction: (T) -> Unit,
) {
    val context = LocalContext.current

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .blockShadow(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(
                    items = options.entries.toList(),
                    key = { it.key.toString() },
                ) {
                    ActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = it.value ?: stringResource(id = R.string.unknown),
                    ) {
                        onAction(it.key)
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                FlatActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.cancel)
                ) {
                    openDialogCustom.value = false
                }
            }
        }
    }
}
