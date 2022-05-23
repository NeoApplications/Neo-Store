package com.saggitt.omega.compose.components

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.LocalScaffoldPadding
import com.saggitt.omega.util.addIf
import kotlinx.coroutines.awaitCancellation

@Composable
fun PreferenceLazyColumn(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: LazyListState = rememberLazyListState(),
    isChild: Boolean = false,
    content: LazyListScope.() -> Unit
) {
    if (!enabled) {
        LaunchedEffect(key1 = null) {
            state.scroll(scrollPriority = MutatePriority.PreventUserInput) {
                awaitCancellation()
            }
        }
    }
    ConsumeScaffoldPadding { contentPadding ->
        NestedScrollStretch {
            LazyColumn(
                modifier = modifier
                    .addIf(!isChild) {
                        fillMaxHeight()
                    },
                contentPadding = rememberExtendPadding(
                    contentPadding,
                    bottom = if (isChild) 0.dp else 16.dp
                ),
                state = state,
                content = content
            )
        }
    }
}

@Composable
fun ConsumeScaffoldPadding(
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val contentPadding = LocalScaffoldPadding.current
    CompositionLocalProvider(
        LocalScaffoldPadding provides PaddingValues(0.dp)
    ) {
        content(contentPadding)
    }
}