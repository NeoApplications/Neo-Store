package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Installed
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.Order
import com.looker.droidify.entity.Section
import com.looker.droidify.ui.fragments.Request
import com.looker.droidify.ui.fragments.Source
import com.looker.droidify.utility.extension.ManageableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainNavFragmentViewModelX(
    val db: DatabaseX,
    primarySource: Source,
    secondarySource: Source
) : ViewModel() {
    // TODO add better sort/filter fields

    var order = MutableLiveData(Order.LAST_UPDATE)
        private set
    var sections = MutableLiveData<Section>(Section.All)
        private set
    var searchQuery = MutableLiveData("")
        private set

    fun request(source: Source): Request {
        var mSearchQuery = ""
        var mSections: Section = Section.All
        var mOrder: Order = Order.NAME
        sections.value?.let { if (source.sections) mSections = it }
        order.value?.let { if (source.order) mOrder = it }
        searchQuery.value?.let { mSearchQuery = it }
        return when (source) {
            Source.AVAILABLE -> Request.ProductsAll(
                mSearchQuery,
                mSections,
                mOrder
            )
            Source.INSTALLED -> Request.ProductsInstalled(
                mSearchQuery,
                mSections,
                mOrder
            )
            Source.UPDATES -> Request.ProductsUpdates(
                mSearchQuery,
                mSections,
                mOrder
            )
            Source.UPDATED -> Request.ProductsUpdated(
                mSearchQuery,
                mSections,
                Order.LAST_UPDATE,
                Preferences[Preferences.Key.UpdatedApps]
            )
            Source.NEW -> Request.ProductsNew(
                mSearchQuery,
                mSections,
                Order.DATE_ADDED,
                Preferences[Preferences.Key.NewApps]
            )
        }
    }

    private var primaryRequest = MediatorLiveData<Request>()
    val primaryProducts = ManageableLiveData<List<Product>>()
    private var secondaryRequest = request(secondarySource)
    val secondaryProducts = MediatorLiveData<List<Product>>()

    val repositories = MediatorLiveData<List<Repository>>()
    val categories = MediatorLiveData<List<String>>()
    val installed = MediatorLiveData<List<Installed>>()

    init {
        primaryProducts.addSource(
            db.productDao.queryLiveList(primaryRequest.value ?: request(primarySource))
        ) {
            primaryProducts.updateValue(it, System.currentTimeMillis())
        }
        secondaryProducts.addSource(
            db.productDao.queryLiveList(secondaryRequest),
            secondaryProducts::setValue
        )
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
        categories.addSource(db.categoryDao.allNamesLive, categories::setValue)
        listOf(sections, order, searchQuery).forEach {
            primaryRequest.addSource(it) {
                val newRequest = request(primarySource)
                if (primaryRequest.value != newRequest)
                    primaryRequest.value = newRequest
            }
        }
        primaryProducts.addSource(primaryRequest) { request ->
            val updateTime = System.currentTimeMillis()
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    primaryProducts.updateValue(
                        db.productDao.queryObject(request),
                        updateTime
                    )
                }
            }
        }
        installed.addSource(db.installedDao.allLive) {
            if (primarySource == Source.INSTALLED && secondarySource == Source.UPDATES) {
                val updateTime = System.currentTimeMillis()
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        secondaryProducts.postValue(db.productDao.queryObject(secondaryRequest))
                        primaryProducts.updateValue(
                            db.productDao.queryObject(
                                primaryRequest.value ?: request(primarySource)
                            ),
                            updateTime
                        )
                    }
                }
            }
            installed.postValue(it)
        }
    }

    class Factory(
        val db: DatabaseX,
        private val primarySource: Source,
        private val secondarySource: Source
    ) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainNavFragmentViewModelX::class.java)) {
                return MainNavFragmentViewModelX(db, primarySource, secondarySource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
