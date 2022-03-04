package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Category
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentExploreXBinding
import com.looker.droidify.entity.Section
import com.looker.droidify.ui.compose.ProductsVerticalRecycler
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.isDarkTheme
import com.looker.droidify.widget.FocusSearchView

class ExploreFragment : MainNavFragmentX() {

    private lateinit var binding: FragmentExploreXBinding

    override val primarySource = Source.AVAILABLE
    override val secondarySource = Source.AVAILABLE

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentExploreXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun setupAdapters() {
    }

    override fun setupLayout() {
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
        viewModel.primaryProducts.observe(viewLifecycleOwner) {
            binding.primaryComposeRecycler.setContent {
                AppTheme(
                    darkTheme = when (Preferences[Preferences.Key.Theme]) {
                        is Preferences.Theme.System -> isSystemInDarkTheme()
                        is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                        else -> isDarkTheme
                    }
                ) {
                    Scaffold { _ ->
                        ProductsVerticalRecycler(it, repositories,
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
        viewModel.categories.observe(viewLifecycleOwner) {
            binding.categories.apply {
                removeAllViews()
                addView(Chip(requireContext(), null, R.attr.categoryChipStyle).apply {
                    setText(R.string.all_applications)
                    id = R.id.SHOW_ALL
                })
                it.sorted().forEach {
                    addView(Chip(requireContext(), null, R.attr.categoryChipStyle).apply {
                        text = it
                    })
                }
                val selectedSection = viewModel.sections.value
                check(
                    children.filterNotNull()
                        .find { it is Chip && selectedSection is Category && it.text == selectedSection.name }?.id
                        ?: R.id.SHOW_ALL
                )
            }
        }
        binding.categories.setOnCheckedChangeListener { group, checkedId ->
            group.findViewById<Chip>(checkedId).let {
                viewModel.setSection(
                    if (it.text.equals(getString(R.string.all_applications)))
                        Section.All
                    else
                        Section.Category(it.text.toString())
                )
            }
        }
        mainActivityX.menuSetup.observe(viewLifecycleOwner) {
            if (it != null) {
                val searchView =
                    mainActivityX.toolbar.menu.findItem(R.id.toolbar_search).actionView as FocusSearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (isResumed) viewModel.setSearchQuery(newText.orEmpty())
                        return true
                    }
                })
            }
        }
    }
}
