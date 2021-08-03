package com.google.android.apps.nexuslauncher.qsb;

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

    public final boolean useTwoBubbles() {
        // pixel_2018_qsb_use_two_bubbles
        return false;
    }

    public final boolean eg() {
        // pixel_2017_qsb_use_colored_g
        return false;
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
