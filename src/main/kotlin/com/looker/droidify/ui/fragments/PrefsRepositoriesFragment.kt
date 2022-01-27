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
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

        reposFastAdapter?.onClickListener =
            { _: View?, _: IAdapter<RepoItem>?, item: RepoItem?, _: Int? ->
                item?.item?.let {
                    it.enabled = !it.enabled
                    GlobalScope.launch(Dispatchers.IO) {
                        syncConnection.binder?.setEnabled(it, it.enabled)
                    }
                }
                false
            }
        reposFastAdapter?.onLongClickListener =
            { _: View?, _: IAdapter<RepoItem>?, item: RepoItem?, _: Int? ->
                item?.item?.let {
                    RepositorySheetX(it.id).showNow(
                        parentFragmentManager,
                        "Repository ${it.id}"
                    )
                }
                false
            }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reposFastAdapter
        }
        binding.addRepository.setOnClickListener { viewModel.addRepository() }
    }

    override fun setupLayout() {
        viewModel.repositories.observe(requireActivity()) {
            // Function: sync when an enabled repo got edited
            val enabledList = it.filter { it.enabled }
            reposItemAdapter.adapterItems.filter(RepoItem::isEnabled).forEach { item ->
                enabledList.firstOrNull { it.id == item.item.id }?.let { repo ->
                    repo.let { data ->
                        if (data != item.item) syncConnection.binder?.sync(data)
                    }
                }
            }
            reposItemAdapter.set(
                it.sortedBy { repo -> !repo.enabled }
                    .mapNotNull { dbRepo ->
                        RepoItem(dbRepo)
                    }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
    }
}
