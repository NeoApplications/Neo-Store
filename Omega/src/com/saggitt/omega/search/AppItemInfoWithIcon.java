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

import android.content.Intent;

import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.util.ComponentKey;

public class AppItemInfoWithIcon extends ItemInfoWithIcon {

    public Intent mIntent;

    public AppItemInfoWithIcon(ComponentKey componentKey) {
        mIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER").setComponent(componentKey.componentName).addFlags(270532608);
        user = componentKey.user;
    }


    public AppItemInfoWithIcon(AppItemInfoWithIcon appItemInfoWithIcon) {
        super(appItemInfoWithIcon);
    }

    public Intent getIntent() {
        return mIntent;
    }

    @Override
    public AppItemInfoWithIcon clone() {
        return new AppItemInfoWithIcon(this);
    }

}
