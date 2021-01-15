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

package com.saggitt.omega.qsb;

import android.annotation.TargetApi;
import android.content.Context;

import java.util.ArrayList;

@TargetApi(26)
public class QsbConfiguration {
    private static QsbConfiguration INSTANCE;

    private final ArrayList<QsbChangeListener> mListeners = new ArrayList<>(2);

    private QsbConfiguration(Context context) {
    }

    public static QsbConfiguration getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new QsbConfiguration(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public final float micStrokeWidth() {
        // pixel_2018_qsb_mic_stroke_width_dp
        return 0f;
    }

    public final String hintTextValue() {
        // pixel_2017_qsb_hint_text_value
        return "";
    }

    public final boolean hintIsForAssistant() {
        // pixel_2018_qsb_hint_is_for_assistant
        return false;
    }

    public final void addListener(QsbChangeListener qsbChangeListener) {
        mListeners.add(qsbChangeListener);
    }

    public final void removeListener(QsbChangeListener qsbChangeListener) {
        mListeners.remove(qsbChangeListener);
    }
}
