package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsVerticalRecycler
import com.machiav3lli.fdroid.ui.compose.components.CategoryChipList
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.viewmodels.MainNavFragmentViewModelX
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.onLaunchClick

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

    AppTheme(
        darkTheme = when (Preferences[Preferences.Key.Theme]) {
            is Preferences.Theme.System -> isSystemInDarkTheme()
            is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
            else -> isDarkTheme
        }
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            CategoryChipList(
                list = listOf(
                    stringResource(id = R.string.all_applications),
                    stringResource(id = R.string.favorite_applications),
                    *categories.sorted().toTypedArray()
                )
            ) {
                viewModel.sections.postValue(
                    when (it) {
                        context.getString(R.string.all_applications) -> Section.All
                        context.getString(R.string.favorite_applications) -> Section.FAVORITE
                        else -> Section.Category(it)
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
