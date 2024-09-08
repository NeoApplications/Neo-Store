package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.machiav3lli.fdroid.NeoActivity
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NeoNavigationSuiteScaffold(
    pages: ImmutableList<NavItem>,
    selectedPage: NavItem,
    onItemClick: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val mActivity = context as NeoActivity

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED)
            NavigationSuiteType.NavigationRail
        else NavigationSuiteType.NavigationBar
    }
    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onBackground,
            indicatorColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onBackground,
            indicatorColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onBackground,
            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )

    NavigationSuiteScaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        layoutType = customNavSuiteType,
        navigationSuiteItems = {
            pages.forEachIndexed { index, it ->
                navItem(
                    item = it,
                    selected = it == selectedPage,
                    itemColors = itemColors,
                    onClick = {
                        if (customNavSuiteType == NavigationSuiteType.NavigationBar)
                            mActivity.paneNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
                        onItemClick(index)
                    }
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = Color.Transparent,
            navigationRailContainerColor = Color.Transparent,
            navigationDrawerContainerColor = Color.Transparent,
            navigationBarContentColor = MaterialTheme.colorScheme.onBackground,
            navigationRailContentColor = MaterialTheme.colorScheme.onBackground,
            navigationDrawerContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        content = content
    )
}

fun NavigationSuiteScope.navItem(
    item: NavItem,
    selected: Boolean,
    itemColors: NavigationSuiteItemColors,
    onClick: () -> Unit,
) {
    item(
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(id = item.title),
                modifier = Modifier.size(24.dp),
            )
        },
        label = {
            AnimatedVisibility(
                visible = !selected,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Text(
                    text = stringResource(id = item.title),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        selected = selected,
        colors = itemColors,
        onClick = onClick,
    )
}