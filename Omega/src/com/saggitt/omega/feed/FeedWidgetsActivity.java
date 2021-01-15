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

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.saggitt.omega.settings.SettingsBaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FeedWidgetsActivity extends SettingsBaseActivity implements AddedWidgetsAdapter.OnActionClickListener {

    private AppWidgetManager mAppWidgetManager;
    private AppWidgetHost mAppWidgetHost;

    private static final int REQUEST_PICK_APPWIDGET = 455;
    private static final int REQUEST_CREATE_APPWIDGET = 189;

    private Launcher mLauncher;
    private AddedWidgetsAdapter mAddedWidgetsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_feed);

        mLauncher = Launcher.getLauncher(getApplicationContext());

        mAppWidgetManager = AppWidgetManager.getInstance(mLauncher.getApplicationContext());
        mAppWidgetHost = mLauncher.getAppWidgetHost();

        RecyclerView addedWidgets = findViewById(R.id.added_widgets_recycler_view);
        addedWidgets.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        addedWidgets.setHasFixedSize(false);
        addedWidgets.setNestedScrollingEnabled(false);
        addedWidgets.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mAddedWidgetsAdapter = new AddedWidgetsAdapter(this, metrics.densityDpi);
        addedWidgets.setAdapter(mAddedWidgetsAdapter);

        refreshRecyclerView();


        findViewById(R.id.add_widget_button).setOnClickListener(view -> {
            selectWidget();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void refreshRecyclerView() {
        List<Widget> widgets = new ArrayList<>();
        int[] widgetIds = mAppWidgetHost.getAppWidgetIds();
        Arrays.sort(widgetIds);
        for (int id : widgetIds) {
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(id);
            if (appWidgetInfo != null) {
                Widget widget = new Widget();
                widget.id = id;
                widget.info = appWidgetInfo;
                widgets.add(widget);
            }
        }
        mAddedWidgetsAdapter.setAppWidgetProviderInfos(widgets);
    }

    void selectWidget() {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(this, WidgetPicker.class);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    void addEmptyData(Intent pickIntent) {
        ArrayList customInfo = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList customExtras = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                removeWidget(appWidgetId);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo != null && appWidgetInfo.configure != null) {
            /*Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);*/
            startAppWidgetConfigureActivitySafely(appWidgetId);
        } else {
            createWidget(data);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        RoundedWidgetView hostView = (RoundedWidgetView) mAppWidgetHost.createView(
                getApplicationContext(), appWidgetId,
                appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        //WidgetManager.getInstance().enqueueAddWidget(hostView);
        refreshRecyclerView();
    }

    void startAppWidgetConfigureActivitySafely(int appWidgetId) {
        try {
            mAppWidgetHost.startAppWidgetConfigureActivityForResult(this, appWidgetId, 0,
                    REQUEST_CREATE_APPWIDGET, null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void removeWidget(int id) {
        mAppWidgetHost.deleteAppWidgetId(id);

    }
}
