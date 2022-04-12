package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import com.google.android.material.composethemeadapter.MdcTheme
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentInstalledXBinding
import com.looker.droidify.ui.compose.ProductsHorizontalRecycler
import com.looker.droidify.ui.compose.ProductsVerticalRecycler
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.isDarkTheme
import com.looker.droidify.widget.FocusSearchView

class InstalledFragment : MainNavFragmentX() {

    private lateinit var binding: FragmentInstalledXBinding

    override val primarySource = Source.INSTALLED
    override val secondarySource = Source.UPDATES

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentInstalledXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun setupLayout() {
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
        viewModel.installed.observe(viewLifecycleOwner) {}
        viewModel.primaryProducts.observe(viewLifecycleOwner) {
            redrawPage(it)
        }
        viewModel.secondaryProducts.observe(viewLifecycleOwner) {
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
    private fun redrawPage(products: List<Product>?) {
        binding.primaryComposeRecycler.setContent {
            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                    else -> isDarkTheme
                }
            ) {
                Scaffold { _ ->
                    ProductsVerticalRecycler(products?.sortedBy(Product::label), repositories,
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
