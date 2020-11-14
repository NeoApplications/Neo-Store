/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saggitt.omega.search;

import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    final AllAppsSearchBarController.Callbacks mCallbacks;
    final String mQuery;
    final ArrayList<ComponentKey> mApps;
    final List<String> mSuggestions;

    SearchResult(String query, AllAppsSearchBarController.Callbacks callbacks) {
        mApps = new ArrayList<>();
        mQuery = query;
        mCallbacks = callbacks;
        mSuggestions = new ArrayList<>();
    }
}
