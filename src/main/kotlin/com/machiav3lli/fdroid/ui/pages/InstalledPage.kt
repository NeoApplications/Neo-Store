package com.machiav3lli.fdroid.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.ProductsVerticalRecycler
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.viewmodels.MainNavFragmentViewModelX
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.onLaunchClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledPage(viewModel: MainNavFragmentViewModelX) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val primaryList by viewModel.primaryProducts.observeAsState(null)
    val secondaryList by viewModel.secondaryProducts.observeAsState(null)
    val installedList by viewModel.installed.observeAsState(null)
    val repositories by viewModel.repositories.observeAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesLive.observeAsState(emptyArray())

    SideEffect {
        CoroutineScope(Dispatchers.IO).launch {
            mainActivityX.searchQuery.collect { newQuery ->
                if (newQuery != viewModel.searchQuery.value)
                    viewModel.searchQuery.postValue(newQuery)
            }
        }
    }

    AppTheme(
        darkTheme = when (Preferences[Preferences.Key.Theme]) {
            is Preferences.Theme.System -> isSystemInDarkTheme()
            is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
            else -> isDarkTheme
        }
    ) {
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
                                painter = painterResource(id = if (updatesVisible) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                                contentDescription = stringResource(id = R.string.updates)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        SuggestionChip(
                            shape = MaterialTheme.shapes.medium,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            onClick = {
                                secondaryList?.let {
                                    mainActivityX.syncConnection.binder?.updateApps(
                                        it.map(
                                            Product::toItem
                                        )
                                    )
                                }
                            },
                            icon = {
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    painter = painterResource(id = R.drawable.ic_download),
                                    contentDescription = stringResource(id = R.string.update_all)
                                )
                            },
                            label = {
                                Text(text = stringResource(id = R.string.update_all))
                            }
                        )
                    }
                    AnimatedVisibility(visible = updatesVisible) {
                        ProductsHorizontalRecycler(secondaryList, repositoriesMap) { item ->
                            mainActivityX.navigateProduct(item.packageName)
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
                SuggestionChip(
                    shape = MaterialTheme.shapes.medium,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    onClick = {
                        mainActivityX.navigateSortFilter(NavItem.Installed.destination)
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(id = R.drawable.ic_sort),
                            contentDescription = stringResource(id = R.string.sort_filter)
                        )
                    },
                    label = {
                        Text(text = stringResource(id = R.string.sort_filter))
                    }
                )
            }
            ProductsVerticalRecycler(
                productsList = primaryList?.sortedBy { it.label.lowercase() },
                repositories = repositoriesMap,
                favorites = favorites,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onUserClick = { item ->
                    mainActivityX.navigateProduct(item.packageName)
                },
                onFavouriteClick = { item ->
                    viewModel.setFavorite(
                        item.packageName,
                        !favorites.contains(item.packageName)
                    )
                },
                getInstalled = { installedList?.get(it.packageName) }
            ) { item ->
                val installed = installedList?.get(item.packageName)
                if (installed != null && installed.launcherActivities.isNotEmpty())
                    context.onLaunchClick(installed, mainActivityX.supportFragmentManager)
                else
                    mainActivityX.syncConnection.binder?.installApps(listOf(item))
            }
        }
    }
}
