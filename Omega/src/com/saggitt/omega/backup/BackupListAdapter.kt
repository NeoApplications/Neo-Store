/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.backup

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.isVisible

class BackupListAdapter(val context: Context) : RecyclerView.Adapter<BackupListAdapter.Holder>() {

    private val backupList = ArrayList<BackupFile>()
    private val backupMetaLoaderList = ArrayList<BackupFile.MetaLoader>()

    var callbacks: Callbacks? = null

    fun setData(data: List<BackupFile>) {
        backupList.clear()
        data.forEach {
            backupList.add(it)
            backupMetaLoaderList.add(BackupFile.MetaLoader(it))
        }
    }

    fun addItem(backupFile: BackupFile) {
        backupList.add(0, backupFile)
        backupMetaLoaderList.add(0, BackupFile.MetaLoader(backupFile))
        notifyItemChanged(0)
    }

    fun removeItem(position: Int) {
        if (backupList[position].delete()) {
            backupList.removeAt(position)
            backupMetaLoaderList.removeAt(position)
            notifyItemRemoved(position + 1)
        } else {
            Toast.makeText(context, R.string.backup_delete_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun toUriList(): List<Uri> {
        return backupList.map { it.uri }
    }

    operator fun get(position: Int) = backupList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(getLayoutId(viewType), parent, false)
        return when (viewType) {
            TYPE_MENU -> MenuHolder(view)
            TYPE_ITEM -> ItemHolder(view)
            else -> Holder(view)
        }
    }

    private fun getLayoutId(viewType: Int) = when (viewType) {
        TYPE_MENU -> R.layout.backup_menu
        TYPE_ITEM -> R.layout.backup_item
        else -> R.layout.backup_blank
    }

    override fun getItemCount() = if (backupList.isEmpty()) 2 else backupList.size + 1

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_MENU
            else -> if (backupList.isEmpty()) TYPE_BLANK else TYPE_ITEM
        }
    }

    fun onDestroy() {
        backupMetaLoaderList.forEach { it.meta?.recycle() }
    }

    open class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(position: Int) {}
    }

    inner class MenuHolder(itemView: View) : Holder(itemView), View.OnClickListener {

        init {
            itemView.findViewById<View>(R.id.action_new_backup).setOnClickListener(this)
            itemView.findViewById<View>(R.id.action_restore_backup).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.local_backup_title)
                .setTextColor(Utilities.getOmegaPrefs(context).accentColor)
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.action_new_backup -> callbacks?.openBackup()
                R.id.action_restore_backup -> callbacks?.openRestore()
            }
        }
    }

    inner class ItemHolder(itemView: View) : Holder(itemView), View.OnClickListener,
        View.OnLongClickListener {

        private val previewContainer = itemView.findViewById<View>(R.id.preview_container)
        private val wallpaper = itemView.findViewById<ImageView>(R.id.wallpaper)
        private val preview = itemView.findViewById<ImageView>(R.id.preview)
        private val title = itemView.findViewById<TextView>(android.R.id.title)
        private val summary = itemView.findViewById<TextView>(android.R.id.summary)

        private val backupItem = itemView.findViewById<View>(R.id.backup_item)

        init {
            backupItem.setOnClickListener(this)
            backupItem.setOnLongClickListener(this)
        }

        override fun bind(position: Int) {
            val metaLoader = backupMetaLoaderList[position - 1]
            if (metaLoader.loaded) {
                previewContainer.isVisible = true
                backupItem.isEnabled = true
                title.text = metaLoader.meta?.name ?: context.getString(R.string.backup_invalid)
                summary.text = metaLoader.meta?.localizedTimestamp
                    ?: context.getString(R.string.backup_invalid)
                metaLoader.meta?.preview?.apply {
                    previewContainer.isVisible = true
                    preview.setImageBitmap(first)
                    wallpaper.setImageBitmap(second)
                }
            } else {
                previewContainer.isVisible = false
                backupItem.isEnabled = false
                title.text = context.getString(R.string.loading)
                summary.text = context.getString(R.string.loading)
                metaLoader.callback = object : BackupFile.MetaLoader.Callback {
                    override fun onMetaLoaded() {
                        notifyItemChanged(backupMetaLoaderList.indexOf(metaLoader) + 1)
                    }
                }
                metaLoader.loadMeta(true)
            }
        }

        override fun onClick(v: View) {
            callbacks?.openRestore(bindingAdapterPosition - 1)
        }

        override fun onLongClick(v: View?): Boolean {
            callbacks?.openEdit(bindingAdapterPosition - 1)
            return true
        }
    }

    interface Callbacks {
        fun openEdit(position: Int)
        fun openRestore(position: Int)
        fun openRestore()
        fun openBackup()
    }

    companion object {
        const val TYPE_MENU = 0
        const val TYPE_ITEM = 1
        const val TYPE_BLANK = 2
    }
}