package com.saggitt.omega.compose.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.google.accompanist.insets.ui.LocalScaffoldPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarUI(
    searchInput: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val searchBarHeight = 56.dp
    val innerPadding = remember { MutablePaddingValues() }
    val searchBarVerticalMargin = 8.dp
    val statusBarHeight = 26.dp
    val contentShift = statusBarHeight + searchBarVerticalMargin + searchBarHeight / 2
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                    .padding(horizontal = 16.dp, vertical = searchBarVerticalMargin)
                    .height(searchBarHeight),
                shape = MaterialTheme.shapes.small,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(searchBarHeight)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    IconButton(
                        onClick = { backDispatcher?.onBackPressed() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.gesture_press_back)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 36.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            searchInput()
                        }
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Row(
                                Modifier.fillMaxHeight(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                                content = actions
                            )
                        }
                    }
                }
            }
        },
        bottomBar = { BottomSpacer() }
    ) {
        val layoutDirection = LocalLayoutDirection.current
        innerPadding.left = it.calculateLeftPadding(layoutDirection)
        innerPadding.top = it.calculateTopPadding() - contentShift
        innerPadding.right = it.calculateRightPadding(layoutDirection)
        innerPadding.bottom = it.calculateBottomPadding()
        CompositionLocalProvider(
            LocalScaffoldPadding provides innerPadding
        ) {
            Box(modifier = Modifier.padding(top = contentShift)) {
                content(it)
            }
        }
    }
}

@Stable
internal class MutablePaddingValues : PaddingValues {
    var left: Dp by mutableStateOf(0.dp)
    var top: Dp by mutableStateOf(0.dp)
    var right: Dp by mutableStateOf(0.dp)
    var bottom: Dp by mutableStateOf(0.dp)

    override fun calculateLeftPadding(layoutDirection: LayoutDirection) = left

    override fun calculateTopPadding(): Dp = top

    override fun calculateRightPadding(layoutDirection: LayoutDirection) = right

    override fun calculateBottomPadding(): Dp = bottom
}