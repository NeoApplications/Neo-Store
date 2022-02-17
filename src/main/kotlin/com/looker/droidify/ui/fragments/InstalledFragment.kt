package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentInstalledXBinding

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
        binding.buttonUpdated.setOnClickListener {
        }

        viewModel.primaryProducts.observe(viewLifecycleOwner) {
        }
        viewModel.secondaryProducts.observe(viewLifecycleOwner) {
            binding.updatedBar.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
    }
}
