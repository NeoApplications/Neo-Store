package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import com.machiav3lli.fdroid.ui.compose.utils.HorizontalExpandingVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    TopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        actions = actions,
    )
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
            ExpandedSearchView(
                query = query,
                modifier = modifier,
                onClose = onClose,
                onExpanded = onExpanded,
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
fun ExpandedSearchView(
    query: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onExpanded: (Boolean) -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    LaunchedEffect(textFieldFocusRequester) { textFieldFocusRequester.requestFocus() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }

    TextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onQueryChanged(it.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(textFieldFocusRequester),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Phosphor.MagnifyingGlass,
                contentDescription = stringResource(id = R.string.search),
            )
        },
        trailingIcon = {
            TopBarAction(
                icon = Phosphor.X,
                description = stringResource(id = R.string.cancel)
            ) {
                textFieldValue = TextFieldValue(text = "")
                focusManager.clearFocus()
                onExpanded(false)
                onClose()
            }
        },
        singleLine = true,
        label = { Text(text = stringResource(id = R.string.search)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}

@Composable
fun WideSearchField(
    modifier: Modifier = Modifier,
    query: String,
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }

    TextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onQueryChanged(it.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(textFieldFocusRequester),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Phosphor.MagnifyingGlass,
                contentDescription = stringResource(id = R.string.search),
            )
        },
        trailingIcon = {
            TopBarAction(
                icon = Phosphor.X,
                description = stringResource(id = R.string.cancel)
            ) {
                textFieldValue = TextFieldValue(text = "")
                onClose()
            }
        },
        singleLine = true,
        label = { Text(text = stringResource(id = R.string.search)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}

@Composable
fun TopBarAction(
    icon: ImageVector,
    description: String = "",
    onClick: (() -> Unit),
) {
    IconButton(onClick = onClick) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = description
        )
    }
}