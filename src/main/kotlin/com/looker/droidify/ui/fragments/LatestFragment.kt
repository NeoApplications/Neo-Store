package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.looker.droidify.R
import com.looker.droidify.database.Product
import com.looker.droidify.databinding.FragmentLatestXBinding
import com.looker.droidify.entity.Repository
import com.looker.droidify.ui.adapters.AppListAdapter
import com.looker.droidify.ui.items.HAppItem
import com.looker.droidify.ui.items.VAppItem
import com.looker.droidify.ui.viewmodels.MainNavFragmentViewModelX
import com.looker.droidify.utility.PRODUCT_ASYNC_DIFFER_CONFIG
import com.looker.droidify.utility.RxUtils
import com.looker.droidify.utility.extension.resources.getDrawableCompat
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.paged.PagedModelAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class LatestFragment : MainNavFragmentX() {

    override lateinit var viewModel: MainNavFragmentViewModelX
    private lateinit var binding: FragmentLatestXBinding

    private lateinit var updatedItemAdapter: PagedModelAdapter<Product, VAppItem>
    private var updatedFastAdapter: FastAdapter<VAppItem>? = null
    private lateinit var newItemAdapter: PagedModelAdapter<Product, HAppItem>
    private var newFastAdapter: FastAdapter<HAppItem>? = null

    // TODO replace the source with one that get a certain amount of updated apps
    override val source = Source.UPDATED

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentLatestXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val viewModelFactory = MainNavFragmentViewModelX.Factory(mainActivityX.db, source)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainNavFragmentViewModelX::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updatedItemAdapter = PagedModelAdapter<Product, VAppItem>(PRODUCT_ASYNC_DIFFER_CONFIG) {
            it.data_item?.let { item ->
                VAppItem(item, repositories[it.repository_id])
            }
        }
        newItemAdapter = PagedModelAdapter<Product, HAppItem>(PRODUCT_ASYNC_DIFFER_CONFIG) {
            it.data_item?.let { item ->
                // TODO filter for only new apps and add placeholder
                HAppItem(item, repositories[it.repository_id])
            }
        }

        updatedFastAdapter = FastAdapter.with(updatedItemAdapter)
        updatedFastAdapter?.setHasStableIds(true)
        binding.updatedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            recycledViewPool.setMaxRecycledViews(AppListAdapter.ViewType.PRODUCT.ordinal, 30)
            adapter = updatedFastAdapter
            FastScrollerBuilder(this)
                .useMd2Style()
                .setThumbDrawable(this.context.getDrawableCompat(R.drawable.scrollbar_thumb))
                .build()
        }
        newFastAdapter = FastAdapter.with(newItemAdapter)
        newFastAdapter?.setHasStableIds(true)
        binding.newRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            recycledViewPool.setMaxRecycledViews(AppListAdapter.ViewType.PRODUCT.ordinal, 30)
            adapter = newFastAdapter
        }
        //viewModel.fillList(source)
        viewModel.db.repositoryDao.allFlowable
            .observeOn(Schedulers.io())
            .flatMapSingle { list -> RxUtils.querySingle { list.mapNotNull { it.trueData } } }
            .map { list -> list.asSequence().map { Pair(it.id, it) }.toMap() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { repositories = it }

        viewModel.productsList.observe(requireActivity()) {
            newItemAdapter.submitList(it)
            updatedItemAdapter.submitList(it)
        }
    }
}
