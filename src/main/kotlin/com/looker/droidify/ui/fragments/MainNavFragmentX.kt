package com.looker.droidify.ui.fragments

import androidx.fragment.app.viewModels
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.ui.activities.MainActivityX
import com.looker.droidify.ui.viewmodels.MainNavFragmentViewModelX

abstract class MainNavFragmentX : BaseNavFragment() {
    val mainActivityX: MainActivityX
        get() = requireActivity() as MainActivityX
    val viewModel: MainNavFragmentViewModelX by viewModels {
        MainNavFragmentViewModelX.Factory(mainActivityX.db, source)
    }
    abstract val source: Source

    open fun onBackPressed(): Boolean = false

    internal fun setSearchQuery(searchQuery: String) {
        viewModel.setSearchQuery(searchQuery) {
            if (view != null) {
                //viewModel.fillList(source)
            }
        }
    }

    internal fun setSection(section: ProductItem.Section) {
        viewModel.setSection(section) {
            if (view != null) {
                //viewModel.fillList(source)
            }
        }
    }

    internal fun setOrder(order: ProductItem.Order) {
        viewModel.setOrder(order) {
            if (view != null) {
                //viewModel.fillList(source)
            }
        }
    }
}

enum class Source(val sections: Boolean, val order: Boolean) {
    AVAILABLE( true, true),
    INSTALLED( false, true),
    UPDATES( false, false),
    UPDATED( false, true),
    NEW( false, true)
}

sealed class Request {
    internal abstract val id: Int
    internal abstract val installed: Boolean
    internal abstract val updates: Boolean
    internal abstract val searchQuery: String
    internal abstract val section: ProductItem.Section
    internal abstract val order: ProductItem.Order
    internal open val numberOfItems: Int = 0

    data class ProductsAll(
        override val searchQuery: String, override val section: ProductItem.Section,
        override val order: ProductItem.Order,
    ) : Request() {
        override val id: Int
            get() = 1
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
    }

    data class ProductsInstalled(
        override val searchQuery: String, override val section: ProductItem.Section,
        override val order: ProductItem.Order,
    ) : Request() {
        override val id: Int
            get() = 2
        override val installed: Boolean
            get() = true
        override val updates: Boolean
            get() = false
    }

    data class ProductsUpdates(
        override val searchQuery: String, override val section: ProductItem.Section,
        override val order: ProductItem.Order,
    ) : Request() {
        override val id: Int
            get() = 3
        override val installed: Boolean
            get() = true
        override val updates: Boolean
            get() = true
    }

    data class ProductsUpdated(
        override val searchQuery: String, override val section: ProductItem.Section,
        override val order: ProductItem.Order, override val numberOfItems: Int,
    ) : Request() {
        override val id: Int
            get() = 4
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
    }

    data class ProductsNew(
        override val searchQuery: String, override val section: ProductItem.Section,
        override val order: ProductItem.Order, override val numberOfItems: Int,
    ) : Request() {
        override val id: Int
            get() = 5
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
    }
}