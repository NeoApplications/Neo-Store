package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentExploreXBinding
import com.looker.droidify.ui.items.VAppItem
import com.looker.droidify.utility.PRODUCT_ASYNC_DIFFER_CONFIG
import com.looker.droidify.utility.RxUtils
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.paged.PagedModelAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

// TODO create categories layouts that hold the apps in horizontal layout
class ExploreFragment : MainNavFragmentX() {

    private lateinit var binding: FragmentExploreXBinding
    private lateinit var appsItemAdapter: PagedModelAdapter<Product, VAppItem>
    private var appsFastAdapter: FastAdapter<VAppItem>? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.db.repositoryDao.allFlowable
            .observeOn(Schedulers.io())
            .flatMapSingle { list -> RxUtils.querySingle { list } }
            .map { list -> list.asSequence().map { Pair(it.id, it) }.toMap() }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { repositories = it }
    }

    override fun setupAdapters() {
        appsItemAdapter = PagedModelAdapter<Product, VAppItem>(PRODUCT_ASYNC_DIFFER_CONFIG) {
            it.data_item?.let { item -> VAppItem(item, repositories[it.repository_id]) }
        }
        appsFastAdapter = FastAdapter.with(appsItemAdapter)
        appsFastAdapter?.setHasStableIds(true)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = appsFastAdapter
        }
    }

    override fun setupLayout() {
        viewModel.primaryProducts.observe(viewLifecycleOwner) {
            appsItemAdapter.submitList(it)
            appsFastAdapter?.notifyDataSetChanged()
        }
    }
}
