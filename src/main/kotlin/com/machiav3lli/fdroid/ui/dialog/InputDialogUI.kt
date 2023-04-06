package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.content.IntPrefsRanges
import com.machiav3lli.fdroid.content.NonBooleanPrefsMeta
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.components.DialogNegativeButton
import com.machiav3lli.fdroid.ui.components.DialogPositiveButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntInputPrefDialogUI(
    prefKey: Preferences.Key<Int>,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val range = IntPrefsRanges[prefKey]
    val nnRange = range ?: 0..1000000
    val textFieldFocusRequester = remember { FocusRequester() }
    var savedValue by remember {
        mutableStateOf(Preferences[prefKey])
    }

    LaunchedEffect(textFieldFocusRequester) {
        delay(100)
        textFieldFocusRequester.requestFocus()
    }

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(NonBooleanPrefsMeta[prefKey] ?: -1),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "${nnRange.first}-${nnRange.last}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(textFieldFocusRequester),
                value = if (savedValue != -1) savedValue.toString()
                else "",
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                placeholder = {
                    Text(text = "${nnRange.first}-${nnRange.last}")
                },
                onValueChange = {
                    savedValue = if (it.isNotEmpty())
                        it.filter { it.isDigit() }
                            .toIntOrNull()
                            ?: prefKey.default.value
                    else -1
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )

            Row(
                Modifier.fillMaxWidth()
            ) {
                DialogNegativeButton(
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        Preferences[prefKey] =
                            savedValue.coerceIn(nnRange)
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringInputPrefDialogUI(
    prefKey: Preferences.Key<String>,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    var savedValue by remember {
        mutableStateOf(Preferences[prefKey])
    }

    LaunchedEffect(textFieldFocusRequester) {
        delay(100)
        textFieldFocusRequester.requestFocus()
    }

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(NonBooleanPrefsMeta[prefKey] ?: -1),
                style = MaterialTheme.typography.titleLarge
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(textFieldFocusRequester),
                value = savedValue,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                onValueChange = { savedValue = it },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )

            Row(
                Modifier.fillMaxWidth()
            ) {
                DialogNegativeButton(
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        if (savedValue.isNotEmpty()) Preferences[prefKey] = savedValue
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}