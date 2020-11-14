/*
 * Copyright (c) 2020 Omega Launcher
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
 */

package com.saggitt.omega.allapps

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.AppFilter
import com.saggitt.omega.OmegaLauncher

open class OmegaAppFilter(context: Context) : AppFilter() {

    private val mHideList = HashSet<ComponentName>()

    init {
        mHideList.add(ComponentName(context, OmegaLauncher::class.java.name))
        //Voice Search
        mHideList.add(ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/.VoiceSearchActivity")!!);

        //Wallpapers
        mHideList.add(ComponentName.unflattenFromString("com.google.android.apps.wallpaper/.picker.CategoryPickerActivity")!!);

        //Google Now Launcher
        mHideList.add(ComponentName.unflattenFromString("com.google.android.launcher/.StubApp")!!);

        //Actions Services
        mHideList.add(ComponentName.unflattenFromString("com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity")!!);

    }

    override fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return !mHideList.contains(componentName) && super.shouldShowApp(componentName, user)
    }
}