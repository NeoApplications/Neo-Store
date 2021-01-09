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

package com.saggitt.omega.icons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.adaptive.IconShape;
import com.saggitt.omega.adaptive.IconShapeDrawable;
import com.saggitt.omega.wallpaper.WallpaperPreviewProvider;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.max;

public class PreviewFrameView extends LinearLayout implements OmegaPreferences.OnPreferenceChangeListener {

    private final Drawable wallpaper;
    private final int[] viewLocation = new int[2];
    private final ImageView[] icons = new ImageView[4];

    private final OmegaPreferences prefs;
    private final String[] prefsToWatch = {"pref_iconShape", "pref_colorizeGeneratedBackgrounds",
            "pref_enableWhiteOnlyTreatment", "pref_enableLegacyTreatment",
            "pref_generateAdaptiveForIconPack", "pref_forceShapeless"};
    private int count = 6;
    private boolean isFirstLoad = true;

    public PreviewFrameView(Context context) {
        this(context, null, 0);
    }

    public PreviewFrameView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewFrameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.HORIZONTAL);
        wallpaper = WallpaperPreviewProvider.Companion.getInstance(context).getWallpaper();
        prefs = Utilities.getOmegaPrefs(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        loadIcons();
        loadBackground(prefs.getForceShapeless());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        prefs.addOnPreferenceChangeListener(this, prefsToWatch);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        prefs.reloadIcons();
        prefs.removeOnPreferenceChangeListener(this, prefsToWatch);
    }

    private void loadBackground(boolean shapeless) {
        Drawable drawable = new IconShapeDrawable(IconShape.Companion.fromString(prefs.getIconShape()));
        if (!shapeless) {
            if (prefs.getEnableWhiteOnlyTreatment()) {
                /*Instagram*/
                Drawable drawable1 = new IconShapeDrawable(IconShape.Companion.fromString(prefs.getIconShape()));
                drawable1.setColorFilter(Color.parseColor("#9f47d2"), PorterDuff.Mode.SRC_IN);
                icons[0].setBackground(drawable1);

                /*Youtube*/
                Drawable drawable2 = new IconShapeDrawable(IconShape.Companion.fromString(prefs.getIconShape()));
                drawable2.setColorFilter(Color.parseColor("#bf1919"), PorterDuff.Mode.SRC_IN);
                icons[1].setBackground(drawable2);

                /*WhatsApp*/
                Drawable drawable3 = new IconShapeDrawable(IconShape.Companion.fromString(prefs.getIconShape()));
                drawable3.setColorFilter(Color.parseColor("#5eea7f"), PorterDuff.Mode.SRC_IN);
                icons[2].setBackground(drawable3);

                /*Photos*/
                Drawable drawable4 = new IconShapeDrawable(IconShape.Companion.fromString(prefs.getIconShape()));
                drawable4.setColorFilter(Color.parseColor("#1c60d8"), PorterDuff.Mode.SRC_IN);
                icons[3].setBackground(drawable4);

            } else {
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                for (ImageView icon : icons) {
                    icon.setBackground(drawable);
                }
            }
        } else {
            for (ImageView icon : icons) {
                icon.setBackground(null);
            }
        }
    }

    private void loadIcons() {
        icons[0] = findViewById(R.id.icon_instagram);
        icons[1] = findViewById(R.id.icon_youtube);
        icons[2] = findViewById(R.id.icon_whatsapp);
        icons[3] = findViewById(R.id.icon_photos);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        int width = wallpaper.getIntrinsicWidth();
        int height = wallpaper.getIntrinsicHeight();
        if (width == 0 || height == 0) {
            super.dispatchDraw(canvas);
            return;
        }

        getLocationInWindow(viewLocation);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float scaleX = (float) dm.widthPixels / width;
        float scaleY = (float) dm.heightPixels / height;
        float scale = max(scaleX, scaleY);

        canvas.save();
        canvas.translate(0f, -(float) viewLocation[1]);
        canvas.scale(scale, scale);
        wallpaper.setBounds(0, 0, width, height);
        wallpaper.draw(canvas);
        canvas.restore();

        super.dispatchDraw(canvas);
    }

    @Override
    public void onValueChanged(@NotNull String key, @NotNull OmegaPreferences prefs, boolean force) {
        if (!isFirstLoad && count == 0) {
            Log.d("IconPreview", "Cambiando preferencia " + key);
            loadIcons();
            loadBackground(prefs.getForceShapeless());
            invalidate();
        } else {
            isFirstLoad = false;
            count--;
        }
    }
}
