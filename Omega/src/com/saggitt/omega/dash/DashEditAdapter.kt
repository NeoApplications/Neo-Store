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

package com.saggitt.omega.dash

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.isVisible

class DashEditAdapter(context: Context) : RecyclerView.Adapter<DashEditAdapter.Holder>() {
    private val prefs = Utilities.getOmegaPrefs(context)
    private val allItems = ArrayList<CustomDashItem>()
    private val handler = Handler()

    private var dividerIndex = 0
    private val adapterItems = ArrayList<Item>()
    private val currentSpecs = ArrayList<String>()
    private val divider = DividerItem()
    private val enabledItems = ArrayList<CustomDashItem>()
    private var isDragging = false
    private val mContext = context
    private var enable: Boolean = false
    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val currentItems = ArrayList<String>()
        val dashItems = ArrayList<CustomDashItem>()

        /*for (action in prefs.dashItems) {
            if (action.length > 1) {
                var item: DashItem? = null
                if (action.length == 2) {
                    item = DashUtils.getDashItemFromString(action)
                } else if (action.length > 2) {
                    /*val keyMapper = ComponentKey(mContext, action)
                    val info = getApp(keyMapper)

                    if (info != null) {
                        item = DashItem.asApp(info.get(0), 99)
                        dashItems.add(CustomDashItem(item))
                    }*/
                }

                if (item != null) {
                    currentItems.add(item.title)
                }
            }
        }

        for (item in DashUtils.actionDisplayItems) {
            dashItems.add(CustomDashItem(item))
        }*/
        currentSpecs.addAll(currentItems)
        allItems.addAll(dashItems)
        fillItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return when (viewType) {
            TYPE_ENABLE -> createHolder(parent, R.layout.dash_enable_item, ::EnableHolder)
            TYPE_HEADER -> createHolder(parent, R.layout.dash_text_item, ::HeaderHolder)
            TYPE_DASH_ITEM -> createHolder(parent, R.layout.dash_dialog_item, ::DashItemHolder)
            TYPE_DIVIDER -> createHolder(parent, R.layout.dash_divider_item, ::DividerHolder)
            else -> throw IllegalArgumentException("type must be either TYPE_TEXT, " +
                    "TYPE_DASH_ITEM, TYPE_DIVIDER or TYPE_ENABLE")
        }
    }

    override fun getItemCount() = adapterItems.count()

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(adapterItems[position])
    }

    override fun getItemViewType(position: Int): Int {
        return adapterItems[position].type
    }

    fun getDashItems(): Set<String> {
        val items = HashSet<String>()
        val iterator = adapterItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item is CustomDashItem) {
                items.add(item.info.id.toString())
                /*if (item.info.viewType == VIEW_TYPE_DASH_ITEM) {
                    items.add(item.info.id.toString())
                } else if (item.info.viewType == VIEW_TYPE_DASH_APP) {
                    items.add(item.info.component)
                }*/

            } else if (item is DividerItem)
                break
            iterator.remove()
        }
        return items
    }

    private fun fillItems() {
        enabledItems.clear()
        enabledItems.addAll(allItems)
        //Fill Adapter
        adapterItems.clear()
        adapterItems.add(EnableItem())
        adapterItems.add(HeaderItem())
        currentSpecs.forEach {
            val item = getAndRemoveOther(it)
            if (item != null) {
                adapterItems.add(item)
            }
        }

        dividerIndex = adapterItems.count()

        adapterItems.add(divider)
        adapterItems.addAll(enabledItems)
    }

    private fun getAndRemoveOther(s: String): CustomDashItem? {
        val iterator = enabledItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.info.title == s) {
                iterator.remove()

                return item
            }
        }
        return null
    }

    private fun move(from: Int, to: Int): Boolean {
        if (to == from) return true
        move(from, to, adapterItems)
        dividerIndex = adapterItems.indexOf(divider)
        return true
    }

    private fun <T> move(from: Int, to: Int, list: MutableList<T>) {
        list.add(to, list.removeAt(from))
        notifyItemMoved(from, to)
    }

    private inline fun createHolder(parent: ViewGroup, resource: Int, creator: (View) -> Holder): Holder {
        return creator(LayoutInflater.from(parent.context).inflate(resource, parent, false))
    }

    abstract class Item {
        abstract val isStatic: Boolean
        abstract val type: Int
    }

    class HeaderItem : Item() {
        override val isStatic = true
        override val type = TYPE_HEADER
    }

    class CustomDashItem(val info: DashModel) : Item() {
        override val isStatic = false
        override val type = TYPE_DASH_ITEM
    }

    class DividerItem : Item() {
        override val isStatic = true
        override val type = TYPE_DIVIDER
    }

    class EnableItem : Item() {
        override val isStatic = true
        override val type = TYPE_ENABLE
    }

    abstract class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(item: Item) {

        }
    }

    inner class EnableHolder(itemView: View) : Holder(itemView) {
        private val switchEnable: SwitchCompat = itemView.findViewById(R.id.enableSwitch)

        @SuppressLint("PrivateResource")
        override fun bind(item: Item) {
            super.bind(item)
            val context = Launcher.mContext
            val states = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_checked), intArrayOf())
            val colors = intArrayOf(androidx.preference.R.color.switch_thumb_normal_material_light, //Normal
                    prefs.accentColor, //checked
                    androidx.preference.R.color.switch_thumb_disabled_material_light//Disabled
            )
            val thstateList = ColorStateList(states, colors)
            CompoundButtonCompat.setButtonTintList(switchEnable, thstateList)

            enable = prefs.dashEnable
            switchEnable.isChecked = enable
            switchEnable.thumbTintList = thstateList
            switchEnable.setText(if (enable) R.string.on else R.string.off)

            switchEnable.setOnCheckedChangeListener { buttonView, isChecked ->
                buttonView.setText(if (isChecked) R.string.on else R.string.off)
                prefs.dashEnable = isChecked
            }
        }
    }

    inner class HeaderHolder(itemView: View) : Holder(itemView) {

        init {
            itemView.findViewById<TextView>(android.R.id.text1).setText(R.string.enabled_icon_packs)
        }
    }

    inner class DashItemHolder(itemView: View) : Holder(itemView), View.OnClickListener, View.OnTouchListener {

        val icon: ImageView = itemView.findViewById(android.R.id.icon)
        val title: TextView = itemView.findViewById(android.R.id.title)
        val summary: TextView = itemView.findViewById(android.R.id.summary)
        private val dragHandle: View = itemView.findViewById(R.id.drag_handle)
        private val dashItem
            get() = adapterItems[adapterPosition] as? CustomDashItem
                    ?: throw IllegalArgumentException("item must be DashItem")

        init {
            itemView.setOnClickListener(this)
            dragHandle.setOnTouchListener(this)
        }

        override fun bind(item: Item) {
            val packDash = item as? CustomDashItem
                    ?: throw IllegalArgumentException("item must be CustomDashItem")
            val drawable = packDash.info.icon
            icon.setImageDrawable(drawable)
            title.text = packDash.info.title
            summary.text = packDash.info.description
            itemView.isClickable = !packDash.isStatic
            dragHandle.isVisible = !packDash.isStatic
        }

        override fun onClick(v: View) {
            val item = dashItem
            if (adapterPosition > dividerIndex) {
                adapterItems.removeAt(adapterPosition)
                adapterItems.add(2, item)
                notifyItemMoved(adapterPosition, 2)
                dividerIndex++
            } else {
                adapterItems.removeAt(adapterPosition)
                adapterItems.add(dividerIndex, item)
                notifyItemMoved(adapterPosition, dividerIndex)
                dividerIndex--
            }
            notifyItemChanged(dividerIndex)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (v == dragHandle && event.actionMasked == MotionEvent.ACTION_DOWN) {
                itemTouchHelper?.startDrag(this)
            }
            return false
        }

    }

    inner class DividerHolder(itemView: View) : Holder(itemView) {

        val text: TextView = itemView.findViewById(android.R.id.text1)

        override fun bind(item: Item) {
            super.bind(item)
            if (isDragging) {
                text.setText(R.string.drag_to_disable_packs)
            } else {
                text.setText(R.string.drag_to_enable_packs)
            }
            text.isVisible = isDragging || dividerIndex != adapterItems.size - 1
        }
    }

    inner class TouchHelperCallback : ItemTouchHelper.Callback() {

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            isDragging = actionState == ItemTouchHelper.ACTION_STATE_DRAG
            handler.post { notifyItemChanged(dividerIndex) }
        }

        override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return target.adapterPosition in 2..dividerIndex
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val item = adapterItems[viewHolder.adapterPosition]
            val dragFlags = if (item.isStatic) 0 else ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return move(viewHolder.adapterPosition, target.adapterPosition)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }
    }

    companion object {
        const val TYPE_ENABLE = 0
        const val TYPE_HEADER = 1
        const val TYPE_DASH_ITEM = 2
        const val TYPE_DIVIDER = 3
    }
}
