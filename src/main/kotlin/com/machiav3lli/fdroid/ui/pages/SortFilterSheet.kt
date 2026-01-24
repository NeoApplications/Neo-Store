package com.machiav3lli.fdroid.ui.pages

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.AndroidVersion
import com.machiav3lli.fdroid.data.entity.ColoringState
import com.machiav3lli.fdroid.data.entity.toAntiFeature
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.ChipsSwitch
import com.machiav3lli.fdroid.ui.components.DeSelectAll
import com.machiav3lli.fdroid.ui.components.ExpandableItemsBlock
import com.machiav3lli.fdroid.ui.components.OutlinedActionButton
import com.machiav3lli.fdroid.ui.components.SelectChip
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowUUpLeft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Check
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SortAscending
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SortDescending
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.viewmodels.MainVM
import kotlinx.coroutines.ExperimentalCoroutinesApi

// TODO add own? viewmodel
@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun SortFilterSheet(
    navPage: String,
    viewModel: MainVM = koinNeoViewModel(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val sortFilterState by viewModel.sortFilterState.collectAsStateWithLifecycle()

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
    val minSDKFilterKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.MinSDKLatest
        NavItem.Installed.destination -> Preferences.Key.MinSDKInstalled
        NavItem.Search.destination    -> Preferences.Key.MinSDKSearch
        else                          -> Preferences.Key.MinSDKExplore // NavItem.Explore
    }
    val targetSDKFilterKey = when (navPage) {
        NavItem.Latest.destination    -> Preferences.Key.TargetSDKLatest
        NavItem.Installed.destination -> Preferences.Key.TargetSDKInstalled
        NavItem.Search.destination    -> Preferences.Key.TargetSDKSearch
        else                          -> Preferences.Key.TargetSDKExplore // NavItem.Explore
    }

    var sortOption by remember(Preferences[sortKey]) {
        mutableStateOf(Preferences[sortKey])
    }
    var sortAscending by remember(Preferences[sortAscendingKey]) {
        mutableStateOf(Preferences[sortAscendingKey])
    }
    val filteredOutRepos = remember(Preferences[reposFilterKey]) {
        mutableStateListOf(*Preferences[reposFilterKey].toTypedArray())
    }
    var filterCategory by remember(Preferences[categoriesFilterKey]) {
        mutableStateOf(Preferences[categoriesFilterKey])
    }
    val filteredAntifeatures = remember(Preferences[antifeaturesFilterKey]) {
        mutableStateListOf(*Preferences[antifeaturesFilterKey].toTypedArray())
    }
    val filteredLicenses = remember(Preferences[licensesFilterKey]) {
        mutableStateListOf(*Preferences[licensesFilterKey].toTypedArray())
    }
    var filterMinSDK by remember(Preferences[minSDKFilterKey]) {
        mutableStateOf(Preferences[minSDKFilterKey])
    }
    var filterTargetSDK by remember(Preferences[targetSDKFilterKey]) {
        mutableStateOf(Preferences[targetSDKFilterKey])
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.action_reset),
                        icon = Phosphor.ArrowUUpLeft,
                        coloring = ColoringState.Negative,
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
                        coloring = ColoringState.Positive,
                        onClick = {
                            Preferences[sortKey] = sortOption
                            Preferences[sortAscendingKey] = sortAscending
                            Preferences[reposFilterKey] = filteredOutRepos.toSet()
                            Preferences[categoriesFilterKey] = filterCategory
                            Preferences[antifeaturesFilterKey] = filteredAntifeatures.toSet()
                            Preferences[licensesFilterKey] = filteredLicenses.toSet()
                            Preferences[minSDKFilterKey] = filterMinSDK
                            Preferences[targetSDKFilterKey] = filterTargetSDK
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
                    DeSelectAll(
                        sortFilterState.enabledRepos.map { it.id.toString() },
                        filteredOutRepos
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        sortFilterState.enabledRepos.sortedBy { it.name }.forEach {
                            val checked by derivedStateOf {
                                !filteredOutRepos.contains(it.id.toString())
                            }

                            SelectChip(
                                text = it.name,
                                checked = checked,
                            ) {
                                if (checked) filteredOutRepos.add(it.id.toString())
                                else filteredOutRepos.remove(it.id.toString())
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
                        (listOf(Pair(FILTER_CATEGORY_ALL, stringResource(id = R.string.all))) +
                                sortFilterState.categories.sortedBy { it.label }
                                    .map { Pair(it.name, it.label) })
                            .forEach {
                                SelectChip(
                                    text = it.second,
                                    checked = it.first == filterCategory,
                                    alwaysShowIcon = false,
                                ) {
                                    filterCategory = it.first
                                }
                            }
                    }
                }
            }
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.min_android),
                    preExpanded = filterMinSDK != AndroidVersion.Unknown,
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AndroidVersion.entries.forEach {
                            SelectChip(
                                text = it.valueString,
                                checked = it == filterMinSDK,
                                alwaysShowIcon = false,
                            ) {
                                filterMinSDK = it
                            }
                        }
                    }
                }
            }
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.target_android),
                    preExpanded = filterTargetSDK != AndroidVersion.Unknown,
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AndroidVersion.entries.forEach {
                            SelectChip(
                                text = it.valueString,
                                checked = it == filterTargetSDK,
                                alwaysShowIcon = false,
                            ) {
                                filterTargetSDK = it
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
                    DeSelectAll(
                        sortFilterState.antifeaturePairs.map { it.first },
                        filteredAntifeatures
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        sortFilterState.antifeaturePairs.sortedBy {
                            it.second.nullIfEmpty()
                                ?: it.first.toAntiFeature()
                                    ?.let { context.getString(it.titleResId) }
                                ?: it.first
                        }.forEach {
                            val checked by derivedStateOf {
                                !filteredAntifeatures.contains(it.first)
                            }

                            SelectChip(
                                text = it.second.nullIfEmpty()
                                    ?: it.first.toAntiFeature()
                                        ?.let { stringResource(id = it.titleResId) }
                                    ?: it.first,
                                checked = checked
                            ) {
                                if (checked) filteredAntifeatures.add(it.first)
                                else filteredAntifeatures.remove(it.first)
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
                    DeSelectAll(sortFilterState.licenses, filteredLicenses)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        sortFilterState.licenses.sorted().forEach {
                            val checked by derivedStateOf {
                                !filteredLicenses.contains(it)
                            }

                            SelectChip(
                                text = it,
                                checked = checked
                            ) {
                                if (checked) filteredLicenses.add(it)
                                else filteredLicenses.remove(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
