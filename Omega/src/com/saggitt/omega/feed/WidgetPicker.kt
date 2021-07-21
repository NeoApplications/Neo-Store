/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.feed

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import java.text.Collator
import java.util.*

class WidgetPicker : AppCompatActivity(), WidgetPickerAdapter.OnClickListener {
    private var mAppWidgetId = 0
    private var mAppWidgetManager: AppWidgetManager? = null
    private var mPackageManager: PackageManager? = null
    private var mBaseIntent: Intent? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        mPackageManager = packageManager
        mAppWidgetManager = AppWidgetManager.getInstance(this)
        super.onCreate(savedInstanceState)

        // Set default return data
        setResultData(RESULT_CANCELED, null)

        // Read the appWidgetId passed our direction, otherwise bail if not found
        val intent = intent
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        } else {
            finish()
        }

        // Read base intent from extras, otherwise assume default
        val parcel = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_INTENT)
        if (parcel is Intent) {
            mBaseIntent = parcel
            mBaseIntent!!.flags = mBaseIntent!!.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION).inv()
        } else {
            mBaseIntent = Intent(Intent.ACTION_MAIN, null)
            mBaseIntent!!.addCategory(Intent.CATEGORY_DEFAULT)
        }
        setContentView(R.layout.activity_widget_picker)
        val recyclerView = findViewById<RecyclerView>(R.id.all_widgets_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        val adapter = WidgetPickerAdapter(this)
        recyclerView.adapter = adapter
        val items: MutableList<WidgetPickerAdapter.Item> = ArrayList()
        putInstalledAppWidgets(items)
        Collections.sort(items, object : Comparator<WidgetPickerAdapter.Item> {
            var mCollator = Collator.getInstance()
            override fun compare(
                lhs: WidgetPickerAdapter.Item,
                rhs: WidgetPickerAdapter.Item
            ): Int {
                return mCollator.compare(lhs.label, rhs.label)
            }
        })
        adapter.setItems(items)
    }

    private fun putInstalledAppWidgets(items: MutableList<WidgetPickerAdapter.Item>) {
        val installed = mAppWidgetManager!!.installedProviders ?: return
        val size = installed.size
        for (i in 0 until size) {
            val info = installed[i]
            items.add(createItem(info))
        }
    }

    fun createItem(info: AppWidgetProviderInfo): WidgetPickerAdapter.Item {
        val label: CharSequence = info.loadLabel(mPackageManager)
        var icon: Drawable? = null
        if (info.icon != 0) {
            try {
                val res = resources
                val iconDensity: Int = when (val density = res.displayMetrics.densityDpi) {
                    DisplayMetrics.DENSITY_MEDIUM -> DisplayMetrics.DENSITY_LOW
                    DisplayMetrics.DENSITY_TV -> DisplayMetrics.DENSITY_MEDIUM
                    DisplayMetrics.DENSITY_HIGH -> DisplayMetrics.DENSITY_MEDIUM
                    DisplayMetrics.DENSITY_XHIGH -> DisplayMetrics.DENSITY_HIGH
                    DisplayMetrics.DENSITY_XXHIGH -> DisplayMetrics.DENSITY_XHIGH
                    else ->                         // The density is some abnormal value.  Return some other
                        // abnormal value that is a reasonable scaling of it.
                        (density * 0.75f + .5f).toInt()
                }
                val packageResources =
                    mPackageManager!!.getResourcesForApplication(info.provider.packageName)
                icon = packageResources.getDrawableForDensity(info.icon, iconDensity, null)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(
                    TAG, "Can't load icon drawable 0x" + Integer.toHexString(info.icon)
                            + " for provider: " + info.provider
                )
            }
            if (icon == null) {
                Log.w(
                    TAG, "Can't load icon drawable 0x" + Integer.toHexString(info.icon)
                            + " for provider: " + info.provider
                )
            }
        }
        val item = WidgetPickerAdapter.Item(label, icon)
        item.packageName = info.provider.packageName
        item.className = info.provider.className
        item.profile = info.profile
        return item
    }

    override fun onClick(item: WidgetPickerAdapter.Item?) {
        val intent = item?.getIntent(mBaseIntent)
        var result = -10
        if (item?.extras != null) {
            setResultData(RESULT_OK, intent)
        } else {
            try {
                var options: Bundle? = null
                if (intent?.extras != null) {
                    options = intent.extras!!.getBundle(
                        AppWidgetManager.EXTRA_APPWIDGET_OPTIONS
                    )
                }
                val success = mAppWidgetManager!!.bindAppWidgetIdIfAllowed(
                    mAppWidgetId,
                    intent?.component, options
                )
                if (success) {
                    result = RESULT_OK
                } else {
                    val permissionIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    permissionIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                    permissionIntent.putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                        ComponentName(item?.packageName ?: "", item?.className ?: "")
                    )
                    intent?.putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE,
                        item.profile
                    )
                    // TODO: we need to make sure that this accounts for the options bundle.
                    // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                    startActivityForResult(permissionIntent, REQUEST_BIND_APPWIDGET)
                }
            } catch (e: IllegalArgumentException) {
                result = RESULT_CANCELED
            }
            setResultData(result, null)
        }
        finish()
    }

    /**
     * Convenience method for setting the result code and intent. This method
     * correctly injects the [AppWidgetManager.EXTRA_APPWIDGET_ID] that
     * most hosts expect returned.
     */
    private fun setResultData(code: Int, intent: Intent?) {
        val result = intent ?: Intent()
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(code, result)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(
            TAG, "onActivityResult() called with: requestCode = [" + requestCode
                    + "], resultCode = [" + resultCode + "], data = [" + data + "]"
        )
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (resultCode == RESULT_OK) {
                setResultData(RESULT_OK, null)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                setResultData(RESULT_CANCELED, null)
            }
            finish()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val REQUEST_BIND_APPWIDGET = 111
        private const val TAG = "WidgetPicker"
    }
}