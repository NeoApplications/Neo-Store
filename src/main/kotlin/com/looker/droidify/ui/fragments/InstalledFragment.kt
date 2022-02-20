package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import com.google.android.material.composethemeadapter.MdcTheme
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentInstalledXBinding
import com.looker.droidify.ui.compose.ProductsHorizontalRecycler
import com.looker.droidify.ui.compose.ProductsVerticalRecycler
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.utility.isDarkTheme

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
        viewModel.secondaryProducts.observe(viewLifecycleOwner) {
            binding.updatedBar.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            binding.secondaryComposeRecycler.setContent {
                AppTheme(
                    darkTheme = when (Preferences[Preferences.Key.Theme]) {
                        is Preferences.Theme.System -> isSystemInDarkTheme()
                        is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                        else -> isDarkTheme
                    }
                ) {
                    MdcTheme {
                        ProductsHorizontalRecycler(it, repositories) { item ->
                            AppSheetX(item.packageName)
                                .showNow(parentFragmentManager, "Product ${item.packageName}")
                        }
                    }
                }
            }
        }
        binding.buttonUpdated.setOnClickListener {
            binding.secondaryComposeRecycler.visibility =
                when (binding.secondaryComposeRecycler.visibility) {
                    View.VISIBLE -> {
                        binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, 0, R.drawable.ic_arrow_down, 0
                        )
                        View.GONE
                    }
                    else -> {
                        binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, 0, R.drawable.ic_arrow_up, 0
                        )
                        View.VISIBLE
                    }
                }
        }
    }
}
