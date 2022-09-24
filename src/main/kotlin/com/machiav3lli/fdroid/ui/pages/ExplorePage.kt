package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsVerticalRecycler
import com.machiav3lli.fdroid.ui.compose.components.CategoryChip
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.viewmodels.MainNavFragmentViewModelX
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.onLaunchClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ExplorePage(viewModel: MainNavFragmentViewModelX) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val products by viewModel.primaryProducts.observeAsState(null)
    val categories by viewModel.categories.observeAsState(emptyList())
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
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var favoriteFilter by remember {
                    mutableStateOf(false)
                }
                Spacer(modifier = Modifier.weight(1f))
                CategoryChip(
                    category = stringResource(id = R.string.favorite_applications),
                    isSelected = favoriteFilter,
                    onSelected = {
                        favoriteFilter = !favoriteFilter
                        viewModel.sections.postValue(
                            if (it) Section.FAVORITE
                            else Section.All
                        )
                    }
                )
            }
            ProductsVerticalRecycler(
                productsList = products,
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
