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

package com.saggitt.omega.feed;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WidgetPicker extends Activity implements WidgetPickerAdapter.OnClickListener {

    private static final int REQUEST_BIND_APPWIDGET = 111;
    private int mAppWidgetId;

    private AppWidgetManager mAppWidgetManager;
    private PackageManager mPackageManager;

    private static final String TAG = "WidgetPicker";
    private Intent mBaseIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPackageManager = getPackageManager();
        mAppWidgetManager = AppWidgetManager.getInstance(this);

        super.onCreate(savedInstanceState);

        // Set default return data
        setResultData(RESULT_CANCELED, null);

        // Read the appWidgetId passed our direction, otherwise bail if not found
        final Intent intent = getIntent();
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        } else {
            finish();
        }

        // Read base intent from extras, otherwise assume default
        Parcelable parcel = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (parcel instanceof Intent) {
            mBaseIntent = (Intent) parcel;
            mBaseIntent.setFlags(mBaseIntent.getFlags() & ~(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION));
        } else {
            mBaseIntent = new Intent(Intent.ACTION_MAIN, null);
            mBaseIntent.addCategory(Intent.CATEGORY_DEFAULT);
        }

        setContentView(R.layout.activity_widget_picker);
        RecyclerView recyclerView = findViewById(R.id.all_widgets_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        WidgetPickerAdapter adapter = new WidgetPickerAdapter(this);
        recyclerView.setAdapter(adapter);
        List<WidgetPickerAdapter.Item> items = new ArrayList<>();
        putInstalledAppWidgets(items);
        Collections.sort(items, new Comparator<WidgetPickerAdapter.Item>() {
            Collator mCollator = Collator.getInstance();

            public int compare(WidgetPickerAdapter.Item lhs, WidgetPickerAdapter.Item rhs) {
                return mCollator.compare(lhs.getLabel(), rhs.getLabel());
            }
        });
        adapter.setItems(items);
    }

    private void putInstalledAppWidgets(List<WidgetPickerAdapter.Item> items) {
        List<AppWidgetProviderInfo> installed =
                mAppWidgetManager.getInstalledProviders();
        if (installed == null) return;
        final int size = installed.size();
        for (int i = 0; i < size; i++) {
            AppWidgetProviderInfo info = installed.get(i);
            items.add(createItem(info));
        }
    }

    public WidgetPickerAdapter.Item createItem(AppWidgetProviderInfo info) {
        CharSequence label = info.loadLabel(mPackageManager);
        Drawable icon = null;
        if (info.icon != 0) {
            try {
                final Resources res = getResources();
                final int density = res.getDisplayMetrics().densityDpi;
                int iconDensity;
                switch (density) {
                    case DisplayMetrics.DENSITY_MEDIUM:
                        iconDensity = DisplayMetrics.DENSITY_LOW;
                        break;
                    case DisplayMetrics.DENSITY_TV:
                        iconDensity = DisplayMetrics.DENSITY_MEDIUM;
                        break;
                    case DisplayMetrics.DENSITY_HIGH:
                        iconDensity = DisplayMetrics.DENSITY_MEDIUM;
                        break;
                    case DisplayMetrics.DENSITY_XHIGH:
                        iconDensity = DisplayMetrics.DENSITY_HIGH;
                        break;
                    case DisplayMetrics.DENSITY_XXHIGH:
                        iconDensity = DisplayMetrics.DENSITY_XHIGH;
                        break;
                    default:
                        // The density is some abnormal value.  Return some other
                        // abnormal value that is a reasonable scaling of it.
                        iconDensity = (int) ((density * 0.75f) + .5f);
                }
                Resources packageResources = mPackageManager.
                        getResourcesForApplication(info.provider.getPackageName());
                icon = packageResources.getDrawableForDensity(info.icon, iconDensity);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Can't load icon drawable 0x" + Integer.toHexString(info.icon)
                        + " for provider: " + info.provider);
            }
            if (icon == null) {
                Log.w(TAG, "Can't load icon drawable 0x" + Integer.toHexString(info.icon)
                        + " for provider: " + info.provider);
            }
        }
        WidgetPickerAdapter.Item item = new WidgetPickerAdapter.Item(label, icon);
        item.packageName = info.provider.getPackageName();
        item.className = info.provider.getClassName();
        item.profile = info.getProfile();
        return item;
    }

    @Override
    public void onClick(WidgetPickerAdapter.Item item) {
        Intent intent = item.getIntent(mBaseIntent);
        int result = -10;
        if (item.extras != null) {
            setResultData(RESULT_OK, intent);
        } else {
            try {
                Bundle options = null;
                if (intent.getExtras() != null) {
                    options = intent.getExtras().getBundle(
                            AppWidgetManager.EXTRA_APPWIDGET_OPTIONS);
                }

                boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(mAppWidgetId,
                        intent.getComponent(), options);
                if (success) {
                    result = RESULT_OK;
                } else {
                    Intent permissionIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                    permissionIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    permissionIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                            new ComponentName(item.packageName, item.className));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE,
                            item.profile);
                    // TODO: we need to make sure that this accounts for the options bundle.
                    // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                    startActivityForResult(permissionIntent, REQUEST_BIND_APPWIDGET);
                }

            } catch (IllegalArgumentException e) {
                result = RESULT_CANCELED;
            }
            setResultData(result, null);
        }
        finish();
    }

    /**
     * Convenience method for setting the result code and intent. This method
     * correctly injects the {@link AppWidgetManager#EXTRA_APPWIDGET_ID} that
     * most hosts expect returned.
     */
    void setResultData(int code, Intent intent) {
        Intent result = intent != null ? intent : new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(code, result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode
                + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_OK) {
                setResultData(RESULT_OK, null);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                setResultData(RESULT_CANCELED, null);
            }

            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
