package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.*
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.Order
import com.looker.droidify.entity.Section
import com.looker.droidify.ui.fragments.Request
import com.looker.droidify.ui.fragments.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainNavFragmentViewModelX(
    val db: DatabaseX,
    primarySource: Source,
    secondarySource: Source
) : ViewModel() {

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
        searchQuery.value?.let { if (source.sections) mSearchQuery = it }
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
            // TODO differentiate between updated and new (e.g. number of releases)
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
    val primaryProducts = MediatorLiveData<List<Product>>()
    private var secondaryRequest = request(secondarySource)
    val secondaryProducts = MediatorLiveData<List<Product>>()

    val repositories = MediatorLiveData<List<Repository>>()
    val categories = MediatorLiveData<List<String>>()

    init {
        primaryProducts.addSource(
            db.productDao.queryLiveList(primaryRequest.value ?: request(primarySource)),
            primaryProducts::setValue
        )
        secondaryProducts.addSource(
            db.productDao.queryLiveList(secondaryRequest),
            secondaryProducts::setValue
        )
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
        categories.addSource(db.categoryDao.allNamesLive, categories::setValue)
        listOf(sections, order, searchQuery).forEach {
            primaryRequest.addSource(it) {
                primaryRequest.value = request(primarySource)
            }
        }
        primaryProducts.addSource(primaryRequest) { request ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    primaryProducts.postValue(
                        db.productDao.queryObject(
                            request
                        )
                    )
                }
            }
        }
    }

    fun setSection(newSection: Section) {
        viewModelScope.launch {
            if (newSection != sections.value) {
                sections.value = newSection
            }
        }
    }

    fun setOrder(newOrder: Order) {
        viewModelScope.launch {
            if (newOrder != order.value) {
                order.value = newOrder
            }
        }
    }

    fun setSearchQuery(newSearchQuery: String) {
        viewModelScope.launch {
            if (newSearchQuery != searchQuery.value) {
                searchQuery.value = newSearchQuery
            }
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
