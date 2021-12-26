package com.looker.droidify.ui.items

import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import coil.transform.RoundedCornersTransformation
import com.looker.droidify.R
import com.looker.droidify.databinding.ItemAppHorizXBinding
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.entity.Repository
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.utility.Utils
import com.looker.droidify.utility.extension.resources.toPx
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class HAppItem(val item: ProductItem, val repository: Repository) :
    AbstractBindingItem<ItemAppHorizXBinding>() {
    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?)
            : ItemAppHorizXBinding = ItemAppHorizXBinding.inflate(inflater, parent, false)

    override fun bindView(binding: ItemAppHorizXBinding, payloads: List<Any>) {
        val (progressIcon, defaultIcon) = Utils.getDefaultApplicationIcons(binding.icon.context)

        binding.name.text = item.name
        binding.icon.load(
            CoilDownloader.createIconUri(
                binding.icon, item.packageName,
                item.icon, item.metadataIcon, repository
            )
        ) {
            transformations(RoundedCornersTransformation(4.toPx))
            placeholder(progressIcon)
            error(defaultIcon)
        }
        binding.version.text = if (item.canUpdate) item.version else item.installedVersion
    }
}