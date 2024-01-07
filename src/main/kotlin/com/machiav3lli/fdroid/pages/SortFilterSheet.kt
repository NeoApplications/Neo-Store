package com.machiav3lli.fdroid.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Licenses
import com.machiav3lli.fdroid.entity.AntiFeature
import com.machiav3lli.fdroid.index.RepositoryUpdater.db
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.ChipsSwitch
import com.machiav3lli.fdroid.ui.components.SelectChip
import com.machiav3lli.fdroid.ui.components.privacy.ExpandableItemsBlock
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowUUpLeft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Check
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SortAscending
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SortDescending
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.navigation.NavItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun SortFilterSheet(navPage: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val dbHandler = MainApplication.db
    val repos by dbHandler.getRepositoryDao().getAllFlow().collectAsState(emptyList())
    val categories by db.getCategoryDao().getAllNamesFlow().collectAsState(emptyList())
    val licenses by db.getProductDao().getAllLicensesFlow().mapLatest {
        it.map(Licenses::licenses).flatten().distinct()
    }
        .collectAsState(emptyList())
    val activeRepos by remember(repos) { mutableStateOf(repos.filter { it.enabled }) }

    val sortKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.SortOrderLatest
        NavItem.Installed.destination -> Preferences.Key.SortOrderInstalled
        NavItem.Search.destination    -> Preferences.Key.SortOrderSearch
        else                          -> Preferences.Key.SortOrderExplore // NavItem.Explore
    }
    val sortAscendingKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.SortOrderAscendingLatest
        NavItem.Installed.destination -> Preferences.Key.SortOrderAscendingInstalled
        NavItem.Search.destination    -> Preferences.Key.SortOrderAscendingSearch
        else                          -> Preferences.Key.SortOrderAscendingExplore // NavItem.Explore
    }
    val reposFilterKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.ReposFilterLatest
        NavItem.Installed.destination -> Preferences.Key.ReposFilterInstalled
        NavItem.Search.destination    -> Preferences.Key.ReposFilterSearch
        else                          -> Preferences.Key.ReposFilterExplore // NavItem.Explore
    }
    val categoriesFilterKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.CategoriesFilterLatest
        NavItem.Installed.destination -> Preferences.Key.CategoriesFilterInstalled
        NavItem.Search.destination    -> Preferences.Key.CategoriesFilterSearch
        else                          -> Preferences.Key.CategoriesFilterExplore // NavItem.Explore
    }
    val antifeaturesFilterKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.AntifeaturesFilterLatest
        NavItem.Installed.destination -> Preferences.Key.AntifeaturesFilterInstalled
        NavItem.Search.destination    -> Preferences.Key.AntifeaturesFilterSearch
        else                          -> Preferences.Key.AntifeaturesFilterExplore // NavItem.Explore
    }
    val licensesFilterKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.LicensesFilterLatest
        NavItem.Installed.destination -> Preferences.Key.LicensesFilterInstalled
        NavItem.Search.destination    -> Preferences.Key.LicensesFilterSearch
        else                          -> Preferences.Key.LicensesFilterExplore // NavItem.Explore
    }

    var sortOption by remember(Preferences[sortKey]) {
        mutableStateOf(Preferences[sortKey])
    }
    var sortAscending by remember(Preferences[sortAscendingKey]) {
        mutableStateOf(Preferences[sortAscendingKey])
    }
    val filteredOutRepos by remember(Preferences[reposFilterKey]) {
        mutableStateOf(Preferences[reposFilterKey].toMutableSet())
    }
    var filterCategory by remember(Preferences[categoriesFilterKey]) {
        mutableStateOf(Preferences[categoriesFilterKey])
    }
    val filteredAntifeatures by remember(Preferences[antifeaturesFilterKey]) {
        mutableStateOf(Preferences[antifeaturesFilterKey].toMutableSet())
    }
    val filteredLicenses by remember(Preferences[licensesFilterKey]) {
        mutableStateOf(Preferences[licensesFilterKey].toMutableSet())
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.action_reset),
                        icon = Phosphor.ArrowUUpLeft,
                        positive = false
                    ) {
                        Preferences[sortKey] = sortKey.default.value
                        Preferences[sortAscendingKey] = sortAscendingKey.default.value
                        Preferences[reposFilterKey] = reposFilterKey.default.value
                        Preferences[categoriesFilterKey] = categoriesFilterKey.default.value
                        Preferences[antifeaturesFilterKey] = antifeaturesFilterKey.default.value
                        Preferences[licensesFilterKey] = licensesFilterKey.default.value
                        onDismiss()
                    }
                    ActionButton(
                        text = stringResource(id = R.string.action_apply),
                        icon = Phosphor.Check,
                        modifier = Modifier.weight(1f),
                        positive = true,
                        onClick = {
                            Preferences[sortKey] = sortOption
                            Preferences[sortAscendingKey] = sortAscending
                            Preferences[reposFilterKey] = filteredOutRepos
                            Preferences[categoriesFilterKey] = filterCategory
                            Preferences[antifeaturesFilterKey] = filteredAntifeatures
                            Preferences[licensesFilterKey] = filteredLicenses
                            onDismiss()
                        }
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                )
                .blockBorder()
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            if (sortKey != Preferences.Key.SortOrderLatest) item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.sorting_order),
                    preExpanded = true,
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        sortKey.default.value.values.forEach {
                            SelectChip(
                                text = stringResource(id = it.order.titleResId),
                                checked = it == sortOption,
                                alwaysShowIcon = false,
                            ) {
                                sortOption = it
                            }
                        }
                    }

                    ChipsSwitch(
                        firstTextId = R.string.sort_ascending,
                        firstIcon = Phosphor.SortAscending,
                        secondTextId = R.string.sort_descending,
                        secondIcon = Phosphor.SortDescending,
                        firstSelected = sortAscending,
                        onCheckedChange = { checked ->
                            sortAscending = checked
                        }
                    )
                }
            }
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.repositories),
                    preExpanded = filteredOutRepos.isNotEmpty(),
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        activeRepos.sortedBy { it.name }.forEach {
                            var checked by remember {
                                mutableStateOf(
                                    !filteredOutRepos.contains(it.id.toString())
                                )
                            }

                            SelectChip(
                                text = it.name,
                                checked = checked,
                            ) {
                                checked = !checked
                                if (checked)
                                    filteredOutRepos.remove(it.id.toString())
                                else
                                    filteredOutRepos.add(it.id.toString())
                            }
                        }
                    }
                }
            }
            if (categoriesFilterKey != Preferences.Key.CategoriesFilterExplore) item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.categories),
                    preExpanded = filterCategory != FILTER_CATEGORY_ALL,
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        (listOf(FILTER_CATEGORY_ALL) + categories.sorted()).forEach {
                            SelectChip(
                                text = it,
                                checked = it == filterCategory,
                                alwaysShowIcon = false,
                            ) {
                                filterCategory = it
                            }
                        }
                    }
                }
            }
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.allowed_anti_features),
                    preExpanded = filteredAntifeatures.isNotEmpty(),
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AntiFeature.entries.sortedBy { context.getString(it.titleResId) }
                            .forEach {
                                var checked by remember {
                                    mutableStateOf(
                                        !filteredAntifeatures.contains(it.key)
                                    )
                                }

                                SelectChip(
                                    text = stringResource(id = it.titleResId),
                                    checked = checked
                                ) {
                                    checked = !checked
                                    if (checked)
                                        filteredAntifeatures.remove(it.key)
                                    else
                                        filteredAntifeatures.add(it.key)
                                }
                            }
                    }
                }
            }
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.allowed_licenses),
                    preExpanded = filteredLicenses.isNotEmpty(),
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        licenses.sorted().forEach {
                            var checked by remember {
                                mutableStateOf(
                                    !filteredLicenses.contains(it)
                                )
                            }

                            SelectChip(
                                text = it,
                                checked = checked
                            ) {
                                checked = !checked
                                if (checked)
                                    filteredLicenses.remove(it)
                                else
                                    filteredLicenses.add(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
