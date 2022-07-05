package com.machiav3lli.fdroid.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.entity.UpdateCategory
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.viewmodels.MainNavFragmentViewModelX

abstract class MainNavFragmentX : BaseNavFragment() {
    protected val mainActivityX: MainActivityX
        get() = requireActivity() as MainActivityX
    val viewModel: MainNavFragmentViewModelX by viewModels {
        MainNavFragmentViewModelX.Factory(mainActivityX.db, primarySource, secondarySource)
    }
    abstract val primarySource: Source
    abstract val secondarySource: Source

    open fun onBackPressed(): Boolean = false

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
    internal abstract val updateCategory: UpdateCategory
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
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.ALL
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
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.ALL
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
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.ALL
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
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.UPDATED
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
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.NEW
    }
}