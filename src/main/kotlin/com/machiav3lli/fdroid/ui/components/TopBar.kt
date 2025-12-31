package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
    TopBarAction(
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
    if ((query.isNotEmpty() || hasFocus.value) && isExpanded) TopBarAction(
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
    focusOnCompose: Boolean = true,
    showCloseButton: Boolean = false,
    onClose: () -> Unit = {},
    onCleanQuery: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    LaunchedEffect(textFieldFocusRequester) { if (focusOnCompose) textFieldFocusRequester.requestFocus() }

    var textFieldValue by remember {
        mutableStateOf(query)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onQueryChanged(it)
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(textFieldFocusRequester),
            shape = MaterialTheme.shapes.extraLarge,
            trailingIcon = {
                AnimatedVisibility(
                    visible = textFieldValue.isNotEmpty(),
                    enter = expandHorizontally(expandFrom = Alignment.Start),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start),
                ) {
                    TopBarAction(
                        icon = Phosphor.ArrowUUpLeft,
                        description = stringResource(id = R.string.cancel)
                    ) {
                        textFieldValue = ""
                        onCleanQuery()
                    }
                }
            },
            singleLine = true,
            label = { Text(text = label) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        if (showCloseButton) TopBarAction(
            modifier = Modifier.padding(top = 8.dp),
            icon = Phosphor.X,
            description = stringResource(id = R.string.cancel)
        ) {
            textFieldValue = ""
            onCleanQuery()
            onClose()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopBarAction(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    description: String = "",
    onLongClick: (() -> Unit) = {},
    onClick: (() -> Unit),
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description
        )
    }
}