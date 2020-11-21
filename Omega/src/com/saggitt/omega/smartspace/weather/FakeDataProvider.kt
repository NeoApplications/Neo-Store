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

/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.smartspace.weather

import android.text.TextUtils
import androidx.annotation.Keep
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.smartspace.weather.icons.WeatherIconProvider
import com.saggitt.omega.util.Temperature

@Keep
class FakeDataProvider(controller: OmegaSmartspaceController) : OmegaSmartspaceController.DataProvider(controller) {

    private val iconProvider = WeatherIconProvider(controller.context)
    private val weather = OmegaSmartspaceController.WeatherData(iconProvider.getIcon("-1"),
            Temperature(0, Temperature.Unit.Celsius), "")
    private val card = OmegaSmartspaceController.CardData(iconProvider.getIcon("-1"),
            "Title", TextUtils.TruncateAt.END, "Subtitle", TextUtils.TruncateAt.END)

    init {
        updateData(weather, card)
    }
}
