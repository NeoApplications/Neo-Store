/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
    override fun onRemove(item: WorkspaceItemInfo?) = Unit
    override fun onTitleChanged(title: CharSequence?) = Unit
}
