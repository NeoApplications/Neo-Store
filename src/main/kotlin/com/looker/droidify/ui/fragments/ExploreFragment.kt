package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import com.google.android.material.chip.Chip
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentExploreXBinding
import com.looker.droidify.entity.Section
import com.looker.droidify.ui.compose.ProductsVerticalRecycler
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.isDarkTheme


// TODO add chips bar to navigate categories
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
                        ProductsVerticalRecycler(it, repositories) { item ->
                            AppSheetX(item.packageName)
                                .showNow(parentFragmentManager, "Product ${item.packageName}")
                        }
                    }
                }
            }
        }
        viewModel.categories.observe(viewLifecycleOwner) {
            binding.categories.removeAllViews()
            binding.categories.addView(Chip(requireContext(), null, R.attr.chipStyle).apply {
                setText(R.string.all_applications)
            })
            it.forEach {
                binding.categories.addView(Chip(requireContext(), null, R.attr.chipStyle).apply {
                    text = it
                })
            }
        }
    }
}
