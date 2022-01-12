package com.looker.droidify.ui.fragments

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.looker.droidify.R
import com.looker.droidify.database.CursorOwner
import com.looker.droidify.databinding.FragmentLatestXBinding
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.entity.Repository
import com.looker.droidify.ui.items.HAppItem
import com.looker.droidify.ui.items.VAppItem
import com.looker.droidify.ui.viewmodels.MainNavFragmentViewModelX
import com.looker.droidify.utility.RxUtils
import com.looker.droidify.utility.extension.resources.getDrawableCompat
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class LatestFragment : MainNavFragmentX(), CursorOwner.Callback {

    override lateinit var viewModel: MainNavFragmentViewModelX
    private lateinit var binding: FragmentLatestXBinding

    private val updatedItemAdapter = ItemAdapter<VAppItem>()
    private var updatedFastAdapter: FastAdapter<VAppItem>? = null
    private val newItemAdapter = ItemAdapter<HAppItem>()
    private var newFastAdapter: FastAdapter<HAppItem>? = null

    override val source = Source.AVAILABLE

    private var repositories: Map<Long, Repository> = mapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentLatestXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val viewModelFactory = MainNavFragmentViewModelX.Factory(mainActivityX.db)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainNavFragmentViewModelX::class.java)

        updatedFastAdapter = FastAdapter.with(updatedItemAdapter)
        updatedFastAdapter?.setHasStableIds(true)
        binding.updatedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            isMotionEventSplittingEnabled = false
            isVerticalScrollBarEnabled = false
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
            isMotionEventSplittingEnabled = false
            isVerticalScrollBarEnabled = false
            adapter = newFastAdapter
            FastScrollerBuilder(this)
                .useMd2Style()
                .setThumbDrawable(this.context.getDrawableCompat(R.drawable.scrollbar_thumb))
                .build()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel.fillList(source)
        viewModel.db.repositoryDao.allFlowable
            .observeOn(Schedulers.io())
            .flatMapSingle { list -> RxUtils.querySingle { list.mapNotNull { it.trueData } } }
            .map { list -> list.asSequence().map { Pair(it.id, it) }.toMap() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { repositories = it }
    }

    override fun onCursorData(request: CursorOwner.Request, cursor: Cursor?) {
        // TODO get a list instead of the cursor
        // TODO use LiveData and observers instead of listeners
        val appItemList: List<ProductItem> = listOf()
        updatedItemAdapter.set(appItemList  // .filter { !it.hasOneRelease }
            .map { VAppItem(it, repositories[it.repositoryId]) }
        )
        newItemAdapter.set(appItemList // .filter { it.hasOneRelease }
            .map { HAppItem(it, repositories[it.repositoryId]) }
        )
        /*
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    emptyText = when {
                        cursor == null -> ""
                        viewModel.searchQuery.first()
                            .isNotEmpty() -> getString(R.string.no_matching_applications_found)
                        else -> getString(R.string.all_applications_up_to_date)
                    }
                }
            }
        */
    }
}
