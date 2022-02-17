package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentLatestXBinding

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

    override fun setupAdapters() {
    }

    override fun setupLayout() {
        viewModel.primaryProducts.observe(viewLifecycleOwner) {
        }
        viewModel.secondaryProducts.observe(viewLifecycleOwner) {
        }
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
    }
}
