package com.machiav3lli.fdroid.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.service.SyncService
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsVerticalRecycler
import com.machiav3lli.fdroid.ui.compose.components.ExpandableSearchAction
import com.machiav3lli.fdroid.ui.compose.components.TopBar
import com.machiav3lli.fdroid.ui.compose.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.pages.home.components.CategoryChipList
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.onLaunchClick

class ExploreFragment : MainNavFragmentX() {

    override val primarySource = Source.AVAILABLE
    override val secondarySource = Source.AVAILABLE

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent { ExplorePage() }
        }
    }

    override fun setupLayout() {
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ExplorePage() {
        val products by viewModel.primaryProducts.observeAsState(null)
        val categories by viewModel.categories.observeAsState(emptyList())
        val installedList by viewModel.installed.observeAsState(null)
        val searchQuery by viewModel.searchQuery.observeAsState("")
        val favorites by mainActivityX.db.extrasDao.favoritesLive.observeAsState(emptyArray())

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
            rememberTopAppBarScrollState()
        ) { true }

        AppTheme(
            darkTheme = when (Preferences[Preferences.Key.Theme]) {
                is Preferences.Theme.System -> isSystemInDarkTheme()
                is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                else -> isDarkTheme
            }
        ) {
            Scaffold(
                // TODO add the topBar to the activity instead of the fragments
                //modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopBar(
                        title = stringResource(id = R.string.application_name),
                        //scrollBehavior = scrollBehavior
                    ) {
                        ExpandableSearchAction(
                            query = searchQuery.orEmpty(),
                            onClose = {
                                viewModel.searchQuery.postValue("")
                            },
                            onQueryChanged = { query ->
                                if (isResumed && query != searchQuery)
                                    viewModel.searchQuery.postValue(query)
                            }
                        )
                        TopBarAction(icon = Icons.Rounded.Sync) {
                            mainActivityX.syncConnection.binder?.sync(SyncService.SyncRequest.MANUAL)
                        }
                        TopBarAction(icon = Icons.Rounded.Settings) {
                            startActivity(Intent(context, PrefsActivityX::class.java))
                        }
                    }
                }
            ) { padding ->
                Column(
                    Modifier
                        .padding(padding)
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
                                getString(R.string.all_applications) -> Section.All
                                getString(R.string.favorite_applications) -> Section.FAVORITE
                                else -> Section.Category(it)
                            }
                        )
                    }
                    ProductsVerticalRecycler(
                        productsList = products,
                        repositories = repositories,
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
                            requireContext().onLaunchClick(installed, childFragmentManager)
                        else
                            mainActivityX.syncConnection.binder?.installApps(listOf(item))
                    }
                }
            }
        }
    }
}
