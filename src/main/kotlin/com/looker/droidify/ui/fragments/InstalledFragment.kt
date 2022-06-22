package com.looker.droidify.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.activities.PrefsActivityX
import com.looker.droidify.ui.compose.ProductsHorizontalRecycler
import com.looker.droidify.ui.compose.ProductsVerticalRecycler
import com.looker.droidify.ui.compose.components.ExpandableSearchAction
import com.looker.droidify.ui.compose.components.TopBar
import com.looker.droidify.ui.compose.components.TopBarAction
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.isDarkTheme
import com.looker.droidify.utility.onLaunchClick

class InstalledFragment : MainNavFragmentX() {

    override val primarySource = Source.INSTALLED
    override val secondarySource = Source.UPDATES

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent { InstalledPage() }
        }
    }

    override fun setupLayout() {
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun InstalledPage() {
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
                var updatesVisible by remember(secondaryList) { mutableStateOf(true) }

                Column(
                    Modifier
                        .padding(padding)
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
                                ProductsHorizontalRecycler(secondaryList, repositories) { item ->
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
                    ProductsVerticalRecycler(
                        productsList = primaryList?.sortedBy(Product::label),
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
