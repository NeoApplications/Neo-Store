package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowUUpLeft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import com.machiav3lli.fdroid.ui.compose.utils.HorizontalExpandingVisibility
import com.machiav3lli.fdroid.ui.compose.utils.addIf
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "",
    withTopBarInsets: Boolean = true,
    navigationAction: @Composable (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    Row(
        modifier = Modifier
            .addIf(withTopBarInsets) {
                windowInsetsPadding(TopAppBarDefaults.windowInsets)
            }
            .height(72.dp)
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        navigationAction()
        title.nullIfEmpty()?.let { title ->
            Text(
                text = title,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(1f),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        actions()
    }
}

@Composable
fun ExpandableSearchAction(
    query: String,
    modifier: Modifier = Modifier,
    expanded: MutableState<Boolean> = mutableStateOf(false),
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val (isExpanded, onExpanded) = remember { expanded }

    HorizontalExpandingVisibility(
        expanded = isExpanded,
        expandedView = {
            WideSearchField(
                query = query,
                modifier = modifier,
                onCleanQuery = onClose,
                //onExpanded = onExpanded,
                onQueryChanged = onQueryChanged
            )
        },
        collapsedView = {
            CollapsedSearchView(
                onExpanded = onExpanded
            )
        }
    )
}

@Composable
fun CollapsedSearchView(
    onExpanded: (Boolean) -> Unit,
) {
    RoundButton(
        icon = Phosphor.MagnifyingGlass,
        description = stringResource(id = R.string.search),
        onClick = { onExpanded(true) }
    )
}

@Composable
fun RowScope.ExpandedSearchView(
    query: String,
    expanded: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    val hasFocus = remember { mutableStateOf(false) }
    val (isExpanded, onExpanded) = remember { expanded }

    var textFieldValue by remember {
        mutableStateOf(query)
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onQueryChanged(it)
        },
        enabled = isExpanded || !hasFocus.value,
        modifier = modifier
            .weight(1f)
            .focusRequester(textFieldFocusRequester)
            .onFocusChanged { focusState ->
                when {
                    focusState.isFocused && !hasFocus.value -> {
                        hasFocus.value = true
                        onExpanded(true)
                    }

                    !focusState.isFocused                   -> {
                        hasFocus.value = false
                    }
                }
            },
        shape = MaterialTheme.shapes.extraLarge,
        singleLine = true,
        label = { Text(text = stringResource(id = R.string.search)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
    if ((query.isNotEmpty() || hasFocus.value) && isExpanded) RoundButton(
        modifier = Modifier.padding(top = 8.dp),
        icon = Phosphor.X,
        description = stringResource(id = R.string.cancel)
    ) {
        textFieldValue = ""
        focusManager.clearFocus()
        onExpanded(false)
        onClose()
    }
}

@Composable
fun WideSearchField(
    query: String,
    modifier: Modifier = Modifier,
    label: String = stringResource(id = R.string.search),
    showCloseButton: Boolean = false,
    inFocusOnLaunch: Boolean = true,
    onClose: () -> Unit = {},
    onCleanQuery: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onDone: () -> Unit = {},
) {
    val textFieldState = rememberTextFieldState(initialText = query)
    val focusRequester = remember { FocusRequester() }
    var focusRequested by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collectLatest { text ->
                onQueryChanged(text)
            }
    }

    LaunchedEffect(Unit) {
        if (inFocusOnLaunch && !focusRequested) {
            focusRequester.requestFocus()
            focusRequested = true
        }
    }

    LaunchedEffect(query) {
        if (textFieldState.text.toString() != query) {
            textFieldState.setTextAndPlaceCursorAtEnd(query)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            state = textFieldState,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            shape = MaterialTheme.shapes.extraLarge,
            trailingIcon = {
                AnimatedVisibility(
                    visible = textFieldState.text.isNotEmpty(),
                    enter = expandHorizontally(expandFrom = Alignment.Start),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start),
                ) {
                    RoundButton(
                        icon = Phosphor.ArrowUUpLeft,
                        description = stringResource(id = R.string.cancel)
                    ) {
                        textFieldState.clearText()
                        onCleanQuery()
                    }
                }
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(text = label) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                showKeyboardOnFocus = inFocusOnLaunch,
            ),
            onKeyboardAction = { performDefaultAction ->
                onDone()
                performDefaultAction()
            },
        )
        if (showCloseButton) RoundButton(
            modifier = Modifier.padding(top = 8.dp),
            icon = Phosphor.X,
            description = stringResource(id = R.string.cancel)
        ) {
            textFieldState.clearText()
            onCleanQuery()
            onClose()
        }
    }
}

@Composable
fun SuggestionsPopup(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
    onClearHistory: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(color = MaterialTheme.colorScheme.outline, width = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            suggestions.forEach { suggestion ->
                ListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    onClick = {
                        onSuggestion(suggestion)
                    },
                    content = {
                        Text(text = suggestion)
                    },
                )
            }
            if (onClearHistory != null) {
                HorizontalDivider(thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClearHistory()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.clear_search_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}