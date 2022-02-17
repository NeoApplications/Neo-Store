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
import kotlinx.coroutines.launch

class MainNavFragmentViewModelX(
    val db: DatabaseX,
    primarySource: Source,
    secondarySource: Source
) :
    ViewModel() {

    private val order = MutableLiveData(Order.LAST_UPDATE)
    private val sections = MutableLiveData<Section>(Section.All)
    private val searchQuery = MutableLiveData("")

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
                Order.LAST_UPDATE,
                Preferences[Preferences.Key.NewApps]
            )
        }
    }

    private var primaryRequest = request(primarySource)
    val primaryProducts = MediatorLiveData<List<Product>>()
    private var secondaryRequest = request(secondarySource)
    val secondaryProducts = MediatorLiveData<List<Product>>()

    val repositories = MediatorLiveData<List<Repository>>()

    init {
        primaryProducts.addSource(
            productsListMediator(primaryRequest),
            primaryProducts::setValue
        )
        secondaryProducts.addSource(
            productsListMediator(secondaryRequest),
            secondaryProducts::setValue
        )
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
    }

    fun setSection(newSection: Section) {
        viewModelScope.launch {
            if (newSection != sections.value) {
                sections.value = newSection
            }
        }
    }

    fun setSection(newOrder: Order) {
        viewModelScope.launch {
            if (newOrder != order.value) {
                order.value = newOrder
            }
        }
    }

    fun setSection(newSearchQuery: String) {
        viewModelScope.launch {
            if (newSearchQuery != searchQuery.value) {
                searchQuery.value = newSearchQuery
            }
        }
    }

    private fun productsListMediator(request: Request) = db.productDao.queryLiveList(
        installed = request.installed,
        updates = request.updates,
        searchQuery = request.searchQuery,
        section = request.section,
        order = request.order,
        numberOfItems = request.numberOfItems
    )

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
