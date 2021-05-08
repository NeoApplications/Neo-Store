/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences

import androidx.appcompat.app.AppCompatActivity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.AppFilter
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.allapps.OmegaAppFilter
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.settings.SettingsActivity
import com.saggitt.omega.views.RecyclerViewFragment

class SelectableAppsActivity : SettingsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun createLaunchFragment(intent: Intent): Fragment {
        return Fragment.instantiate(this, SelectionFragment::class.java.name, intent.extras)
    }

    override fun shouldShowSearch(): Boolean {
        return false
    }

    class SelectionFragment : RecyclerViewFragment(), SelectableAppsAdapter.Callback {

        private var selection: Set<String> = emptySet()
        private var changed = false

        override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
            val arguments = requireArguments()
            val profile = arguments.getParcelable<DrawerTabs.Profile>(KEY_FILTER_IS_WORK)!!
            selection = HashSet(arguments.getStringArrayList(KEY_SELECTION))

            val context = recyclerView.context
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = SelectableAppsAdapter.ofProperty(requireActivity(),
                    ::selection, this, createAppFilter(context, DrawerTabs.getWorkFilter(profile)))
        }

        override fun onDestroy() {
            super.onDestroy()

            val receiver = requireArguments().getParcelable<ResultReceiver>(KEY_CALLBACK)!!
            if (changed) {
                receiver.send(AppCompatActivity.RESULT_OK, Bundle(1).apply {
                    putStringArrayList(KEY_SELECTION, ArrayList(selection))
                })
            } else {
                receiver.send(AppCompatActivity.RESULT_CANCELED, null)
            }
        }

        override fun onResume() {
            super.onResume()

            updateTitle(selection.size)
        }

        override fun onSelectionsChanged(newSize: Int) {
            changed = true
            updateTitle(newSize)
        }

        private fun updateTitle(size: Int) {
            activity?.title = getString(R.string.selected_count, size)
        }
    }

    companion object {

        private const val KEY_SELECTION = "selection"
        private const val KEY_CALLBACK = "callback"
        private const val KEY_FILTER_IS_WORK = "filterIsWork"

        fun start(context: Context, selection: Collection<ComponentKey>,
                  callback: (Collection<ComponentKey>?) -> Unit, profile: DrawerTabs.Profile) {
            val intent = Intent(context, SelectableAppsActivity::class.java).apply {
                putStringArrayListExtra(KEY_SELECTION, ArrayList(selection.map { it.toString() }))
                putExtra(KEY_CALLBACK, object : ResultReceiver(Handler()) {

                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == AppCompatActivity.RESULT_OK) {
                            callback(resultData!!.getStringArrayList(KEY_SELECTION)!!.map {
                                Utilities.makeComponentKey(context, it)
                            })
                        } else {
                            callback(null)
                        }
                    }
                })
                putExtra(KEY_FILTER_IS_WORK, profile)
            }
            context.startActivity(intent)
        }

        private fun createAppFilter(context: Context, predicate: (ComponentKey) -> Boolean): AppFilter {
            return object : AppFilter() {

                val base = OmegaAppFilter(context)

                override fun shouldShowApp(app: ComponentName, user: UserHandle?): Boolean {
                    if (!base.shouldShowApp(app, user)) {
                        return false
                    }
                    return predicate(ComponentKey(app, user ?: Process.myUserHandle()))
                }
            }
        }
    }
}
