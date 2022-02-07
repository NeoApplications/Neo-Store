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
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.paged.PagedModelAdapter

// TODO add chips bar to navigate categories
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

    override fun setupAdapters() {
        appsItemAdapter = PagedModelAdapter<Product, VAppItem>(PRODUCT_ASYNC_DIFFER_CONFIG) {
            VAppItem(it.item, repositories[it.repository_id])
        }
        appsFastAdapter = FastAdapter.with(appsItemAdapter)
        appsFastAdapter?.setHasStableIds(true)
        appsFastAdapter?.onClickListener =
            { _: View?, _: IAdapter<VAppItem>?, item: VAppItem?, _: Int? ->
                item?.item?.let {
                    AppSheetX(it.packageName)
                        .showNow(parentFragmentManager, "Product ${it.packageName}")
                }
                false
            }
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
        viewModel.repositories.observe(viewLifecycleOwner) {
            repositories = it.associateBy { repo -> repo.id }
        }
    }
}
