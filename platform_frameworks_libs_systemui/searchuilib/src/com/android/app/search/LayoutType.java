/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.app.search;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Constants to be used with {@link SearchTarget}.
 */
public class LayoutType {

    @StringDef(value = {
            ICON_SINGLE_VERTICAL_TEXT,
            ICON_HORIZONTAL_TEXT,
            HORIZONTAL_MEDIUM_TEXT,
            SMALL_ICON_HORIZONTAL_TEXT,
            THUMBNAIL,
            ICON_SLICE,
            WIDGET_PREVIEW,
            WIDGET_LIVE,
            PEOPLE_TILE,
            TEXT_HEADER,
            DIVIDER,
            EMPTY_DIVIDER,
            CALCULATOR,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SearchLayoutType {
    }

    //     ------
    //    | icon |
    //     ------
    //      text
    public static final String ICON_SINGLE_VERTICAL_TEXT = "icon";

    // Below three layouts (to be deprecated) and two layouts render
    // {@link SearchTarget}s in following layout.
    //     ------                            ------   ------
    //    |      | title                    |(opt)|  |(opt)|
    //    | icon | subtitle (optional)      | icon|  | icon|
    //     ------                            ------  ------
    @Deprecated
    public static final String ICON_SINGLE_HORIZONTAL_TEXT = "icon_text_row";
    @Deprecated
    public static final String ICON_DOUBLE_HORIZONTAL_TEXT = "icon_texts_row";
    @Deprecated
    public static final String ICON_DOUBLE_HORIZONTAL_TEXT_BUTTON = "icon_texts_button";

    // will replace ICON_DOUBLE_* ICON_SINGLE_* layouts
    public static final String ICON_HORIZONTAL_TEXT = "icon_row";
    public static final String HORIZONTAL_MEDIUM_TEXT = "icon_row_medium";
    public static final String SMALL_ICON_HORIZONTAL_TEXT = "short_icon_row";

    // This layout creates square thumbnail image (currently 3 column)
    public static final String THUMBNAIL = "thumbnail";

    // This layout contains an icon and slice
    public static final String ICON_SLICE = "slice";

    // Widget bitmap preview
    public static final String WIDGET_PREVIEW = "widget_preview";

    // Live widget search result
    public static final String WIDGET_LIVE = "widget_live";

    // Layout type used to display people tiles using shortcut info
    public static final String PEOPLE_TILE = "people_tile";

    // text based header to group various layouts in low confidence section of the results.
    public static final String TEXT_HEADER = "header";

    // horizontal bar to be inserted between fallback search results and low confidence section
    public static final String DIVIDER = "divider";

    // horizontal bar to be inserted between fallback search results and low confidence section
    public static final String EMPTY_DIVIDER = "empty_divider";

    // layout representing quick calculations
    public static final String CALCULATOR = "calculator";
}
