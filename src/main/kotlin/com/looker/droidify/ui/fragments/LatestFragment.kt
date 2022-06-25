package com.looker.droidify.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.activities.PrefsActivityX
import com.looker.droidify.ui.compose.ProductsHorizontalRecycler
import com.looker.droidify.ui.compose.components.ExpandableSearchAction
import com.looker.droidify.ui.compose.components.ProductsListItem
import com.looker.droidify.ui.compose.components.TopBar
import com.looker.droidify.ui.compose.components.TopBarAction
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.isDarkTheme
import com.looker.droidify.utility.onLaunchClick

class LatestFragment : MainNavFragmentX() {

    override val primarySource = Source.UPDATED
    override val secondarySource = Source.NEW

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent { LatestPage() }
        }
    }

    override fun setupLayout() {
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LatestPage() {
        val primaryList by viewModel.primaryProducts.observeAsState(null)
        val secondaryList by viewModel.secondaryProducts.observeAsState(null)
        val installedList by viewModel.installed.observeAsState(null)
        val searchQuery by viewModel.searchQuery.observeAsState("")
        val favorites by mainActivityX.db.extrasDao.favoritesLive.observeAsState(emptyArray())

        AppTheme(
            darkTheme = when (Preferences[Preferences.Key.Theme]) {
                is Preferences.Theme.System -> isSystemInDarkTheme()
                is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                else -> isDarkTheme
            }
        ) {
            Scaffold(
                // TODO add the topBar to the activity instead of the fragments
                topBar = {
                    TopBar(title = stringResource(id = R.string.application_name)) {
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
                LazyColumn(
                    Modifier
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize(),
                ) {
                    item {
                        Text(
                            text = stringResource(id = R.string.new_applications),
                            modifier = Modifier.padding(8.dp)
                        )
                        ProductsHorizontalRecycler(secondaryList, repositories) { item ->
                            mainActivityX.navigateProduct(item.packageName)
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.recently_updated),
                                modifier = Modifier.weight(1f),
                            )
                            SuggestionChip(
                                shape = MaterialTheme.shapes.medium,
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                onClick = { }, // TODO add sort & filter
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
                    }
                    items(
                        items = primaryList?.map { it.toItem() } ?: emptyList(),
                    ) { item ->
                        ProductsListItem(
                            item = item,
                            repo = repositories[item.repositoryId],
                            isFavorite = favorites.contains(item.packageName),
                            onUserClick = { mainActivityX.navigateProduct(it.packageName) },
                            onFavouriteClick = {
                                viewModel.setFavorite(
                                    it.packageName,
                                    !favorites.contains(it.packageName)
                                )
                            },
                            installed = installedList?.get(item.packageName),
                            onActionClick = {
                                val installed = installedList?.get(it.packageName)
                                if (installed != null && installed.launcherActivities.isNotEmpty())
                                    requireContext().onLaunchClick(installed, childFragmentManager)
                                else
                                    mainActivityX.syncConnection.binder?.installApps(listOf(it))
                            }
                        )
                    }
                }
            }
        }
    }
}
