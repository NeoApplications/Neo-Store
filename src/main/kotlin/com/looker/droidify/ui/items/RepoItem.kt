package com.looker.droidify.ui.items

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.looker.droidify.R
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.ItemRepositoryXBinding
import com.looker.droidify.utility.extension.resources.clear
import com.looker.droidify.utility.extension.resources.getColorFromAttr
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class RepoItem(val item: Repository) :
    AbstractBindingItem<ItemRepositoryXBinding>() {
    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?)
            : ItemRepositoryXBinding = ItemRepositoryXBinding.inflate(inflater, parent, false)

    override fun bindView(binding: ItemRepositoryXBinding, payloads: List<Any>) {
        val isEnabled = item.enabled
        val context = binding.repoItem.context

        val textColor: ColorStateList = if (isEnabled)
            context.getColorFromAttr(R.attr.colorOnPrimaryContainer)
        else
            context.getColorFromAttr(R.attr.colorOnBackground)

        binding.repoName.text = item.name
        binding.repoDescription.text = item.description.trim()

        binding.repoItem.setCardBackgroundColor(
            if (item.enabled) context.getColorFromAttr(R.attr.colorPrimaryContainer)
            else context.getColorFromAttr(android.R.attr.colorBackground)
        )

        binding.checkMark.apply {
            if (item.enabled) load(R.drawable.ic_check)
            else clear()
        }

        textColor.let {
            binding.repoName.setTextColor(it)
            binding.repoDescription.setTextColor(it)
            binding.checkMark.imageTintList = it
        }
    }
}