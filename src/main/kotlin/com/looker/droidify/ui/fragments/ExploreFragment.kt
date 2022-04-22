package com.looker.droidify.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentComposeBinding
import com.looker.droidify.entity.Section
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.activities.PrefsActivityX
import com.looker.droidify.ui.compose.ProductsVerticalRecycler
import com.looker.droidify.ui.compose.components.ExpandableSearchAction
import com.looker.droidify.ui.compose.components.TopBar
import com.looker.droidify.ui.compose.components.TopBarAction
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.ui.compose.utils.SelectableChipRow
import com.looker.droidify.utility.isDarkTheme

class ExploreFragment : MainNavFragmentX() {

    private lateinit var binding: FragmentComposeBinding

    override val primarySource = Source.AVAILABLE
    override val secondarySource = Source.AVAILABLE

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentComposeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun setupLayout() {
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
        viewModel.installed.observe(viewLifecycleOwner) {
            // Avoid the compiler using the same class as observer
            Log.d(this::class.java.canonicalName, this.toString())
        }
        viewModel.primaryProducts.observe(viewLifecycleOwner) { products ->
            viewModel.categories.observe(viewLifecycleOwner) { categories ->
                redrawPage(products, categories)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun redrawPage(products: List<Product>?, categories: List<String> = emptyList()) {
        binding.composeView.setContent {
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
                                query = viewModel.searchQuery.value.orEmpty(),
                                onClose = {
                                    viewModel.searchQuery.value = ""
                                },
                                onQueryChanged = { query ->
                                    if (isResumed && query != viewModel.searchQuery.value)
                                        viewModel.setSearchQuery(query)
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
                        SelectableChipRow(
                            list = listOf(
                                stringResource(id = R.string.all_applications),
                                *categories.sorted().toTypedArray()
                            )
                        ) {
                            viewModel.setSection(
                                when (it) {
                                    getString(R.string.all_applications) -> Section.All
                                    else -> Section.Category(it)
                                }
                            )
                        }
                        ProductsVerticalRecycler(
                            products,
                            repositories,
                            Modifier
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
