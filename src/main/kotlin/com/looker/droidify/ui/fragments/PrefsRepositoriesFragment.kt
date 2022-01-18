package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.looker.droidify.databinding.FragmentRepositoriesXBinding
import com.looker.droidify.service.Connection
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.activities.PrefsActivityX
import com.looker.droidify.ui.items.RepoItem
import com.looker.droidify.ui.viewmodels.RepositoriesViewModelX
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class PrefsRepositoriesFragment : BaseNavFragment() {
    private lateinit var binding: FragmentRepositoriesXBinding
    private val reposItemAdapter = ItemAdapter<RepoItem>()
    private var reposFastAdapter: FastAdapter<RepoItem>? = null
    val viewModel: RepositoriesViewModelX by viewModels {
        RepositoriesViewModelX.Factory(prefsActivityX.db)
    }

    private val prefsActivityX: PrefsActivityX
        get() = requireActivity() as PrefsActivityX

    private val syncConnection = Connection(SyncService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentRepositoriesXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun setupAdapters() {
        syncConnection.bind(requireContext())

        reposFastAdapter = FastAdapter.with(reposItemAdapter)
        reposFastAdapter?.setHasStableIds(false)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reposFastAdapter
        }
    }

    override fun setupLayout() {
        viewModel.productsList.observe(requireActivity()) {
            reposItemAdapter.set(
                it.mapNotNull { dbRepo ->
                    dbRepo.trueData?.let { repo ->
                        RepoItem(repo)
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
    }
}
