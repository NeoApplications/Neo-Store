package com.machiav3lli.fdroid.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.ProductsVerticalRecycler
import com.machiav3lli.fdroid.ui.compose.components.ActionChip
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretUp
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.viewmodels.InstalledViewModel
import com.machiav3lli.fdroid.utility.onLaunchClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun InstalledPage(viewModel: InstalledViewModel) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val filteredPrimaryList by viewModel.filteredProducts.collectAsState()
    val secondaryList by viewModel.secondaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(null)
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesFlow.collectAsState(emptyArray())

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            mainActivityX.searchQuery.collect { newQuery ->
                viewModel.setSearchQuery(newQuery)
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ReposFilterInstalled,
                    Preferences.Key.CategoriesFilterInstalled,
                    Preferences.Key.SortOrderInstalled,
                    Preferences.Key.SortOrderAscendingInstalled ->
                        viewModel.setSortFilter(
                            listOf(
                                Preferences[Preferences.Key.ReposFilterInstalled],
                                Preferences[Preferences.Key.CategoriesFilterInstalled],
                                Preferences[Preferences.Key.SortOrderInstalled],
                                Preferences[Preferences.Key.SortOrderAscendingInstalled],
                            ).toString()
                        )
                    else -> {}
                }
            }
        }
    }

    var updatesVisible by remember(secondaryList) { mutableStateOf(true) }

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        AnimatedVisibility(visible = secondaryList.orEmpty().isNotEmpty()) {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedButton(
                        colors = ButtonDefaults.elevatedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        onClick = { updatesVisible = !updatesVisible }
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(id = R.string.updates),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = if (updatesVisible) Phosphor.CaretUp else Phosphor.CaretDown,
                            contentDescription = stringResource(id = R.string.updates)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ActionChip(
                        textId = R.string.update_all,
                        icon = Phosphor.Download,
                    ) {
                        secondaryList?.let {
                            mainActivityX.syncConnection.binder?.updateApps(
                                it.map(
                                    Product::toItem
                                )
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = updatesVisible) {
                    ProductsHorizontalRecycler(
                        modifier = Modifier.weight(1f),
                        productsList = secondaryList,
                        repositories = repositoriesMap,
                        rowsNumber = secondaryList?.size?.coerceIn(1, 2) ?: 1,
                    ) { item ->
                        mainActivityX.navigateProduct(item.packageName, item.developer)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.installed_applications),
                modifier = Modifier.weight(1f),
            )
            ActionChip(
                textId = R.string.sort_filter,
                icon = Phosphor.FunnelSimple
            ) { mainActivityX.navigateSortFilter(NavItem.Installed.destination) }
        }
        ProductsVerticalRecycler(
            productsList = filteredPrimaryList,
            repositories = repositoriesMap,
            favorites = favorites,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onUserClick = { item ->
                mainActivityX.syncConnection.binder?.fetchExodusInfo(item.packageName)
                mainActivityX.navigateProduct(item.packageName, item.developer)
            },
            onFavouriteClick = { item ->
                viewModel.setFavorite(
                    item.packageName,
                    !favorites.contains(item.packageName)
                )
            },
            getInstalled = { packageName -> installedList?.get(packageName) }
        ) { item ->
            val installed = installedList?.get(item.packageName)
            if (installed != null && installed.launcherActivities.isNotEmpty())
                context.onLaunchClick(installed, mainActivityX.supportFragmentManager)
            else
                mainActivityX.syncConnection.binder?.installApps(listOf(item))
        }
    }
}
