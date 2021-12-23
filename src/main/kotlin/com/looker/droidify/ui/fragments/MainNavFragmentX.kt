package com.looker.droidify.ui.fragments

import androidx.fragment.app.Fragment
import com.looker.droidify.R
import com.looker.droidify.database.CursorOwner
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.ui.activities.MainActivityX
import com.looker.droidify.ui.viewmodels.MainNavFragmentViewModelX

abstract class MainNavFragmentX : Fragment(), CursorOwner.Callback {
    val mainActivityX: MainActivityX
        get() = requireActivity() as MainActivityX
    abstract val viewModel: MainNavFragmentViewModelX
    abstract val source: Source

    open fun onBackPressed(): Boolean = false

    internal fun setSearchQuery(searchQuery: String) {
        viewModel.setSearchQuery(searchQuery) {
            if (view != null) {
                mainActivityX.attachCursorOwner(this, viewModel.request(source))
            }
        }
    }

    internal fun setSection(section: ProductItem.Section) {
        viewModel.setSection(section) {
            if (view != null) {
                mainActivityX.attachCursorOwner(this, viewModel.request(source))
            }
        }
    }

    internal fun setOrder(order: ProductItem.Order) {
        viewModel.setOrder(order) {
            if (view != null) {
                mainActivityX.attachCursorOwner(this, viewModel.request(source))
            }
        }
    }
}

enum class Source(val titleResId: Int, val sections: Boolean, val order: Boolean) {
    AVAILABLE(R.string.available, true, true),
    INSTALLED(R.string.installed, false, true),
    UPDATES(R.string.updates, false, false)
}