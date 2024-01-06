package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.IntPrefsRanges
import com.machiav3lli.fdroid.content.NonBooleanPrefsMeta
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.components.DialogNegativeButton
import com.machiav3lli.fdroid.ui.components.DialogPositiveButton
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import com.machiav3lli.fdroid.utility.extension.text.RE_finishChars
import kotlinx.coroutines.delay

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
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
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
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            TextField(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .focusRequester(textFieldFocusRequester),
                value = if (savedValue != -1) savedValue.toString()
                else "",
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                        Preferences[prefKey] =
                            savedValue.coerceIn(nnRange)
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

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
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(NonBooleanPrefsMeta[prefKey] ?: -1),
                style = MaterialTheme.typography.titleLarge
            )
            TextField(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .focusRequester(textFieldFocusRequester),
                value = savedValue,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                        if (savedValue.isNotEmpty()) Preferences[prefKey] = savedValue
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

const val DIALOG_NONE = 0

@Composable
fun StringInputDialogUI(
    titleText: String,
    initValue: String,
    openDialogCustom: MutableState<Boolean>,
    onSave: ((String) -> Unit) = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val mainFocusRequester = remember { FocusRequester() }

    var savedValue by remember {
        mutableStateOf(TextFieldValue(initValue, TextRange(initValue.length)))
    }
    var isEdited by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun submit() {
        focusManager.clearFocus()
        onSave(savedValue.text)
        openDialogCustom.value = false
    }

    SideEffect { mainFocusRequester.requestFocus() }

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .weight(1f, false)
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(mainFocusRequester),
                    value = savedValue,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = MaterialTheme.shapes.large,
                    singleLine = false,
                    onValueChange = {
                        isEdited = true
                        if (it.text.contains(RE_finishChars)) submit()
                        else savedValue = it         // only save when no control char
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { savedValue = TextFieldValue("") }) {
                                Icon(
                                    imageVector = Phosphor.X,
                                    contentDescription = stringResource(id = R.string.clear_text),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    },
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                DialogNegativeButton(textId = R.string.cancel) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(textId = R.string.save) {
                    submit()
                }
            }
        }
    }
}
