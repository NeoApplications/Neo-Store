package com.looker.droidify.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.looker.droidify.R
import com.looker.droidify.entity.Order
import com.looker.droidify.entity.Section
import com.looker.droidify.ui.activities.MainActivityX
import com.looker.droidify.ui.viewmodels.MainNavFragmentViewModelX

abstract class MainNavFragmentX : BaseNavFragment() {
    private val mainActivityX: MainActivityX
        get() = requireActivity() as MainActivityX
    val viewModel: MainNavFragmentViewModelX by viewModels {
        MainNavFragmentViewModelX.Factory(mainActivityX.db, primarySource, secondarySource)
    }
    abstract val primarySource: Source
    abstract val secondarySource: Source

    open fun onBackPressed(): Boolean = false

    internal fun setSearchQuery(searchQuery: String) {
        viewModel.setSearchQuery(searchQuery) {
            if (view != null) {
                //viewModel.fillList(source)
            }
        }
    }

    internal fun setSection(section: Section) {
        viewModel.setSection(section) {
            if (view != null) {
                //viewModel.fillList(source)
            }
        }
    }

    internal fun setOrder(order: Order) {
        viewModel.setOrder(order) {
            if (view != null) {
                //viewModel.fillList(source)
            }
        }
    }

    protected fun launchFragment(fragment: Fragment): Boolean {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_content, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }
}

enum class Source(val sections: Boolean, val order: Boolean) {
    AVAILABLE(true, true),
    INSTALLED(false, true),
    UPDATES(false, false),
    UPDATED(false, true),
    NEW(false, true)
}

sealed class Request {
    internal abstract val id: Int
    internal abstract val installed: Boolean
    internal abstract val updates: Boolean
    internal abstract val searchQuery: String
    internal abstract val section: Section
    internal abstract val order: Order
    internal open val numberOfItems: Int = 0

    data class ProductsAll(
        override val searchQuery: String, override val section: Section,
        override val order: Order,
    ) : Request() {
        override val id: Int
            get() = 1
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
    }

    data class ProductsInstalled(
        override val searchQuery: String, override val section: Section,
        override val order: Order,
    ) : Request() {
        override val id: Int
            get() = 2
        override val installed: Boolean
            get() = true
        override val updates: Boolean
            get() = false
    }

    data class ProductsUpdates(
        override val searchQuery: String, override val section: Section,
        override val order: Order,
    ) : Request() {
        override val id: Int
            get() = 3
        override val installed: Boolean
            get() = true
        override val updates: Boolean
            get() = true
    }

    data class ProductsUpdated(
        override val searchQuery: String, override val section: Section,
        override val order: Order, override val numberOfItems: Int,
    ) : Request() {
        override val id: Int
            get() = 4
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
    }

    data class ProductsNew(
        override val searchQuery: String, override val section: Section,
        override val order: Order, override val numberOfItems: Int,
    ) : Request() {
        override val id: Int
            get() = 5
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
    }
}