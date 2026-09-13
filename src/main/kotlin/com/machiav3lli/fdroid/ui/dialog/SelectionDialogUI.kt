package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.NonBooleanPrefsMeta
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.content.PrefsEntries
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.DialogNegativeButton
import com.machiav3lli.fdroid.ui.components.DialogPositiveButton
import com.machiav3lli.fdroid.ui.components.FlatActionButton
import com.machiav3lli.fdroid.ui.components.MultiSelectionListItem
import com.machiav3lli.fdroid.ui.components.SingleSelectionListItem
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.ui.compose.utils.blockShadow
import com.machiav3lli.fdroid.utils.Utils
import com.machiav3lli.fdroid.utils.Utils.getLocaleOfCode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentList

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
                text = stringResource(NonBooleanPrefsMeta[prefKey]?.first ?: -1),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 4.dp)
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
    var selected by remember { mutableStateOf(Preferences[prefKey]) }
    val possibleValues = prefKey.default.value.values
    val entryPairs = PrefsEntries[prefKey]?.entries?.filter { it.key in possibleValues }.orEmpty()

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
                text = stringResource(NonBooleanPrefsMeta[prefKey]?.first ?: -1),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 4.dp)
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
fun <T> ActionSelectionDialogUI(
    titleId: Int,
    options: Map<T, String>,
    onAction: (T) -> Unit,
    onDismiss: () -> Unit,
) {
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
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .weight(1f, false),
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
                    text = stringResource(id = R.string.cancel),
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
fun <T> MultiSelectionDialogUI(
    titleText: String,
    entryMap: ImmutableMap<T, String>,
    selectedItems: ImmutableList<T>,
    openDialogCustom: MutableState<Boolean>,
    withSearchBar: Boolean = false,
    onSave: (List<T>) -> Unit,
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(selectedItems) }
    val query = retain { mutableStateOf("") }
    val entryPairs = entryMap.toList()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
            if (withSearchBar) {
                WideSearchField(
                    query = query.value,
                    onQueryChanged = {
                        query.value = it
                    },
                    onCleanQuery = {
                        query.value = ""
                    }
                )
            }
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                items(items = entryPairs.filter {
                    it.second.contains(query.value, true)
                }) { (key, label) ->
                    val isSelected = retain(selected, key) {
                        mutableStateOf(selected.contains(key))
                    }

                    MultiSelectionListItem(
                        text = label,
                        isChecked = isSelected.value,
                    ) {
                        selected = (if (it) selected.plus(key)
                        else selected.minus(key)).toPersistentList()
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DialogNegativeButton(textId = R.string.cancel) {
                    openDialogCustom.value = false
                }
                DialogPositiveButton(textId = R.string.ok) {
                    onSave(selected)
                    openDialogCustom.value = false
                }
            }
        }
    }
}
