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
package com.saggitt.omega.search.webproviders

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.saggitt.omega.search.WebSearchProvider
import com.saggitt.omega.util.locale

class GoogleWebSearchProvider(context: Context) :
    WebSearchProvider(context) {
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_super_g_color, null)!!

    override val packageName: String
        get() = "https://www.google.com/search?q=%s"

    override val suggestionsUrl: String
        get() = "https://www.google.com/complete/search?client=chrome&q=%s&hl=" + context.locale.language

    override val name: String
        get() = context.resources.getString(R.string.web_search_google)
}