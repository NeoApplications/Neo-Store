/*
 *  This file is part of Omega Launcher.
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

package com.saggitt.omega.search;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.SearchAlgorithm;
import com.google.android.apps.nexuslauncher.search.AppSearchProvider;
import com.saggitt.omega.search.webproviders.WebSearchProvider;

import java.util.Collections;
import java.util.List;

public class SearchThread implements SearchAlgorithm, Handler.Callback {
    private static HandlerThread handlerThread;
    private final Handler mHandler;
    private final Context mContext;
    private final Handler mUiHandler;
    private boolean mInterruptActiveRequests;

    public SearchThread(Context context) {
        mContext = context;
        mUiHandler = new Handler(this);
        if (handlerThread == null) {
            handlerThread = new HandlerThread("search-thread", -2);
            handlerThread.start();
        }
        mHandler = new Handler(SearchThread.handlerThread.getLooper(), this);
    }

    private void dj(SearchResult componentList) {
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.APPLICATION_ID + ".appssearch")
                .appendPath(componentList.mQuery)
                .build();

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            int suggestIntentData = cursor.getColumnIndex("suggest_intent_data");
            while (cursor.moveToNext()) {
                componentList.mApps.add(AppSearchProvider.uriToComponent(Uri.parse(cursor.getString(suggestIntentData)), mContext));
            }
        } catch (NullPointerException ignored) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        componentList.mSuggestions.addAll(getSuggestions(componentList.mQuery));
        Message.obtain(mUiHandler, 200, componentList).sendToTarget();
    }

    public void cancel(boolean interruptActiveRequests) {
        mInterruptActiveRequests = interruptActiveRequests;
        mHandler.removeMessages(100);
        if (interruptActiveRequests) {
            mUiHandler.removeMessages(200);
        }
    }

    public void doSearch(String query, AllAppsSearchBarController.Callbacks callback) {
        mHandler.removeMessages(100);
        Message.obtain(mHandler, 100, new SearchResult(query, callback)).sendToTarget();
    }

    private List<String> getSuggestions(String query) {
        SearchProvider provider = SearchProviderController.Companion
                .getInstance(mContext).getSearchProvider();
        if (provider instanceof WebSearchProvider) {
            return ((WebSearchProvider) provider).getSuggestions(query);
        }
        return Collections.emptyList();
    }

    public boolean handleMessage(final Message message) {
        switch (message.what) {
            default: {
                return false;
            }
            case 100: {
                dj((SearchResult) message.obj);
                break;
            }
            case 200: {
                if (!mInterruptActiveRequests) {
                    SearchResult searchResult = (SearchResult) message.obj;
                    searchResult.mCallbacks.onSearchResult(searchResult.mQuery, searchResult.mApps, searchResult.mSuggestions);
                }
                break;
            }
        }
        return true;
    }
}