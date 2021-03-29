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

package com.saggitt.omega.search.webproviders;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaiduWebSearchProvider extends WebSearchProvider {
    public BaiduWebSearchProvider(@NotNull Context context) {
        super(context);
    }

    @NotNull
    @Override
    public Drawable getIcon() {
        return getContext().getResources().getDrawable(R.drawable.ic_baidu);
    }

    @Nullable
    @Override
    protected String getSuggestionsUrl() {
        return "http://suggestion.baidu.com/su?action=opensearch&ie=UTF-8&wd=%s";
    }

    @NotNull
    @Override
    public String getName() {
        return getContext().getResources().getString(R.string.web_search_baidu);
    }

    @NotNull
    @Override
    public String getPackageName() {
        return "https://www.baidu.com/s?wd=%s";
    }
}
