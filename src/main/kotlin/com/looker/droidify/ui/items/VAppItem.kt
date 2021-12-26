package com.looker.droidify.ui.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import coil.load
import coil.transform.RoundedCornersTransformation
import com.looker.droidify.R
import com.looker.droidify.databinding.ItemAppVerticalXBinding
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.entity.Repository
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.utility.Utils
import com.looker.droidify.utility.extension.resources.getColorFromAttr
import com.looker.droidify.utility.extension.resources.sizeScaled
import com.looker.droidify.utility.extension.resources.toPx
import com.looker.droidify.utility.extension.text.nullIfEmpty
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class VAppItem(val item: ProductItem, val repository: Repository) :
    AbstractBindingItem<ItemAppVerticalXBinding>() {
    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?)
            : ItemAppVerticalXBinding = ItemAppVerticalXBinding.inflate(inflater, parent, false)

    override fun bindView(binding: ItemAppVerticalXBinding, payloads: List<Any>) {
        val (progressIcon, defaultIcon) = Utils.getDefaultApplicationIcons(binding.icon.context)

        binding.name.text = item.name
        binding.summary.text =
            if (item.name == item.summary) "" else item.summary
        binding.summary.visibility =
            if (binding.summary.text.isNotEmpty()) View.VISIBLE else View.GONE
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
        binding.status.apply {
            if (item.canUpdate) {
                text = item.version
                if (background == null) {
                    background =
                        ResourcesCompat.getDrawable(
                            binding.root.resources,
                            R.drawable.background_border,
                            context.theme
                        )
                    resources.sizeScaled(6).let { setPadding(it, it, it, it) }
                    backgroundTintList =
                        context.getColorFromAttr(R.attr.colorSecondaryContainer)
                    setTextColor(context.getColorFromAttr(R.attr.colorSecondary))
                }
            } else {
                text = item.installedVersion.nullIfEmpty() ?: item.version
                if (background != null) {
                    setPadding(0, 0, 0, 0)
                    setTextColor(binding.status.context.getColorFromAttr(android.R.attr.colorControlNormal))
                    background = null
                }
            }
        }
        val enabled = item.compatible || item.installedVersion.isNotEmpty()
        sequenceOf(binding.name, binding.status, binding.summary).forEach {
            it.isEnabled = enabled
        }
    }
}