package com.saggitt.omega.folder

import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.WorkspaceItemInfo

class FirstItemProvider(private val info: FolderInfo) : FolderInfo.FolderListener {

    var firstItem: WorkspaceItemInfo? = findFirstItem()
        private set

    init {
        info.addListener(this)
    }

    private fun findFirstItem() = info.contents.minByOrNull { it.rank }

    override fun onItemsChanged(animate: Boolean) {
        firstItem = findFirstItem()
    }

    override fun onAdd(item: WorkspaceItemInfo?, rank: Int) = Unit
    override fun onRemove(item: MutableList<WorkspaceItemInfo>?) = Unit
    override fun onTitleChanged(title: CharSequence?) = Unit
}