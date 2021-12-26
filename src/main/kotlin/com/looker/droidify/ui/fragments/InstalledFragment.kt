package com.looker.droidify.ui.fragments

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.looker.droidify.database.CursorOwner
import com.looker.droidify.databinding.FragmentInstalledXBinding
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.entity.Repository
import com.looker.droidify.ui.items.HAppItem
import com.looker.droidify.ui.items.VAppItem
import com.looker.droidify.ui.viewmodels.MainNavFragmentViewModelX
import com.looker.droidify.utility.RxUtils
import com.looker.droidify.widget.RecyclerFastScroller
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class InstalledFragment : MainNavFragmentX(), CursorOwner.Callback {

    override val viewModel: MainNavFragmentViewModelX by viewModels()
    private lateinit var binding: FragmentInstalledXBinding

    private val installedItemAdapter = ItemAdapter<VAppItem>()
    private var installedFastAdapter: FastAdapter<VAppItem>? = null
    private val updatedItemAdapter = ItemAdapter<HAppItem>()
    private var updatedFastAdapter: FastAdapter<HAppItem>? = null

    override val source = Source.INSTALLED

    private var repositories: Map<Long, Repository> = mapOf()
    private var repositoriesDisposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentInstalledXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        installedFastAdapter = FastAdapter.with(installedItemAdapter)
        installedFastAdapter?.setHasStableIds(true)
        binding.installedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            isMotionEventSplittingEnabled = false
            isVerticalScrollBarEnabled = false
            adapter = installedFastAdapter
            RecyclerFastScroller(this)
        }
        updatedFastAdapter = FastAdapter.with(updatedItemAdapter)
        updatedFastAdapter?.setHasStableIds(true)
        binding.updatedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            isMotionEventSplittingEnabled = false
            isVerticalScrollBarEnabled = false
            adapter = updatedFastAdapter
            RecyclerFastScroller(this)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivityX.attachCursorOwner(this, viewModel.request(source))
        repositoriesDisposable = Observable.just(Unit)
            //.concatWith(Database.observable(Database.Subject.Repositories)) TODO have to be replaced like whole rxJava
            .observeOn(Schedulers.io())
            .flatMapSingle { RxUtils.querySingle { mainActivityX.db.repositoryDao.all.mapNotNull { it.trueData } } }
            .map { list -> list.asSequence().map { Pair(it.id, it) }.toMap() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { repositories = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mainActivityX.detachCursorOwner(this)
        repositoriesDisposable?.dispose()
        repositoriesDisposable = null
    }

    override fun onCursorData(request: CursorOwner.Request, cursor: Cursor?) {
        // TODO get a list instead of the cursor
        // TODO use LiveData and observers instead of listeners
        val appItemList: List<ProductItem> = listOf()
        installedItemAdapter.set(appItemList
            .map { VAppItem(it, repositories[it.repositoryId]) }
        )
        updatedItemAdapter.set(appItemList.filter { it.canUpdate }
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
