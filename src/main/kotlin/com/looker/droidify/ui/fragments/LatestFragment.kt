package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentLatestXBinding
import com.looker.droidify.ui.items.HAppItem
import com.looker.droidify.ui.items.VAppItem
import com.looker.droidify.utility.PRODUCT_ASYNC_DIFFER_CONFIG
import com.looker.droidify.utility.RxUtils
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.paged.PagedModelAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class LatestFragment : MainNavFragmentX() {

    private lateinit var binding: FragmentLatestXBinding

    private lateinit var updatedItemAdapter: PagedModelAdapter<Product, VAppItem>
    private var updatedFastAdapter: FastAdapter<VAppItem>? = null
    private lateinit var newItemAdapter: PagedModelAdapter<Product, HAppItem>
    private var newFastAdapter: FastAdapter<HAppItem>? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.db.repositoryDao.allFlowable
            .observeOn(Schedulers.io())
            .flatMapSingle { list -> RxUtils.querySingle { list } }
            .map { list -> list.asSequence().map { Pair(it.id, it) }.toMap() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { repositories = it }
    }

    override fun setupAdapters() {
        updatedItemAdapter = PagedModelAdapter<Product, VAppItem>(PRODUCT_ASYNC_DIFFER_CONFIG) {
            it.item()?.let { item -> VAppItem(item, repositories[it.repository_id]) }
        }
        newItemAdapter = PagedModelAdapter<Product, HAppItem>(PRODUCT_ASYNC_DIFFER_CONFIG) {
            // TODO filter for only new apps and add placeholder
            it.item()?.let { item -> HAppItem(item, repositories[it.repository_id]) }
        }
        updatedFastAdapter = FastAdapter.with(updatedItemAdapter)
        updatedFastAdapter?.setHasStableIds(true)
        binding.updatedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = updatedFastAdapter
        }
        newFastAdapter = FastAdapter.with(newItemAdapter)
        newFastAdapter?.setHasStableIds(true)
        binding.newRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = newFastAdapter
        }
    }

    override fun setupLayout() {
        viewModel.primaryProducts.observe(viewLifecycleOwner) {
            updatedItemAdapter.submitList(it)
        }
        viewModel.secondaryProducts.observe(viewLifecycleOwner) {
            newItemAdapter.submitList(it)
        }
    }
}
