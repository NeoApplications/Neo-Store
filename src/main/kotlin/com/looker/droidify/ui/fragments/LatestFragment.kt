package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentLatestXBinding
import com.looker.droidify.ui.compose.ProductsHorizontalRecycler
import com.looker.droidify.ui.compose.ProductsVerticalRecycler
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.isDarkTheme
import com.looker.droidify.widget.FocusSearchView

class LatestFragment : MainNavFragmentX() {

    private lateinit var binding: FragmentLatestXBinding

    // TODO replace the source with one that get a certain amount of updated apps
    override val primarySource = Source.UPDATED
    override val secondarySource = Source.NEW

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentLatestXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun setupLayout() {
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
        viewModel.installed.observe(viewLifecycleOwner) {}
        viewModel.primaryProducts.observe(viewLifecycleOwner) {
            redrawPage(it, viewModel.secondaryProducts.value)
        }
        viewModel.secondaryProducts.observe(viewLifecycleOwner) {
            redrawPage(viewModel.primaryProducts.value, it)
        }
        mainActivityX.menuSetup.observe(viewLifecycleOwner) {
            if (it != null) {
                val searchView =
                    mainActivityX.toolbar.menu.findItem(R.id.toolbar_search).actionView as FocusSearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        if (isResumed && query != viewModel.searchQuery.value)
                            viewModel.setSearchQuery(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        if (isResumed && newText != viewModel.searchQuery.value)
                            viewModel.setSearchQuery(newText)
                        return true
                    }
                })
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    private fun redrawPage(primaryList: List<Product>?, secondaryList: List<Product>?) {
        binding.primaryComposeRecycler.setContent {
            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                    else -> isDarkTheme
                }
            ) {
                Scaffold { _ ->
                    Column(
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = stringResource(id = R.string.new_applications),
                            modifier = Modifier.padding(8.dp)
                        )
                        ProductsHorizontalRecycler(secondaryList, repositories) { item ->
                            AppSheetX(item.packageName)
                                .showNow(parentFragmentManager, "Product ${item.packageName}")
                        }
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.recently_updated),
                                modifier = Modifier.weight(1f),
                            )
                            Chip(
                                shape = MaterialTheme.shapes.medium,
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                onClick = { } // TODO add sort & filter
                            ) {
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    painter = painterResource(id = R.drawable.ic_sort),
                                    contentDescription = stringResource(id = R.string.sort_filter)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.sort_filter))
                            }
                        }
                        ProductsVerticalRecycler(primaryList, repositories,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            onUserClick = { item ->
                                AppSheetX(item.packageName)
                                    .showNow(parentFragmentManager, "Product ${item.packageName}")
                            },
                            onFavouriteClick = {},
                            onInstallClick = {
                                mainActivityX.syncConnection.binder?.installApps(listOf(it))
                            }
                        )
                    }
                }
            }
        }
    }
}
