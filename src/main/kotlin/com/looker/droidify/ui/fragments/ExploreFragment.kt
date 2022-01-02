package com.looker.droidify.ui.fragments

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.looker.droidify.R
import com.looker.droidify.database.CursorOwner
import com.looker.droidify.databinding.FragmentExploreXBinding
import com.looker.droidify.ui.adapters.AppListAdapter
import com.looker.droidify.ui.viewmodels.MainNavFragmentViewModelX
import com.looker.droidify.utility.RxUtils
import com.looker.droidify.widget.RecyclerFastScroller
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ExploreFragment : MainNavFragmentX(), CursorOwner.Callback {

    override lateinit var viewModel: MainNavFragmentViewModelX
    private lateinit var binding: FragmentExploreXBinding

    override val source = Source.AVAILABLE

    private var repositoriesDisposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentExploreXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val viewModelFactory = MainNavFragmentViewModelX.Factory(mainActivityX.db)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainNavFragmentViewModelX::class.java)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            isMotionEventSplittingEnabled = false
            isVerticalScrollBarEnabled = false
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(AppListAdapter.ViewType.PRODUCT.ordinal, 30)
            adapter = AppListAdapter { mainActivityX.navigateProduct(it.packageName) }
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
            .subscribe { (binding.recyclerView.adapter as? AppListAdapter)?.repositories = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mainActivityX.detachCursorOwner(this)
        repositoriesDisposable?.dispose()
        repositoriesDisposable = null
    }

    override fun onCursorData(request: CursorOwner.Request, cursor: Cursor?) {
        (binding.recyclerView.adapter as? AppListAdapter)?.apply {
            this.cursor = cursor
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    emptyText = when {
                        cursor == null -> ""
                        viewModel.searchQuery.first()
                            .isNotEmpty() -> getString(R.string.no_matching_applications_found)
                        else -> getString(R.string.no_applications_available)
                    }
                }
            }
        }
    }
}
