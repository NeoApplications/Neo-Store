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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.IntProperty;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.logging.StatsLogUtils.LogContainerProvider;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.Themes;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.predictions.CustomAppPredictor;
import com.saggitt.omega.wallpaper.WallpaperPreviewProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.android.launcher3.icons.GraphicsUtils.setColorAlphaBound;
import static com.android.launcher3.userevent.nano.LauncherLogProto.ContainerType;
import static com.android.launcher3.userevent.nano.LauncherLogProto.Target;
import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static java.lang.Math.max;

public class IconPreview extends LinearLayout implements LogContainerProvider,
        OmegaPreferences.OnPreferenceChangeListener, AllAppsStore.OnUpdateListener {
    private static final IntProperty<IconPreview> TEXT_ALPHA =
            new IntProperty<IconPreview>("textAlpha") {
                @Override
                public void setValue(IconPreview view, int alpha) {
                    view.setTextAlpha(alpha);
                }

                @Override
                public Integer get(IconPreview view) {
                    return view.mIconLastSetTextAlpha;
                }
            };
    private final Drawable wallpaper;
    private final int[] viewLocation = new int[2];
    private final Launcher mLauncher;
    private final ArrayList<ItemInfoWithIcon> mPreviewApps = new ArrayList<>();
    private final List<ComponentKey> mPreviewAppComponents = new ArrayList<>();

    private final int mIconTextColor;
    private final int mIconFullTextAlpha;
    private int mIconLastSetTextAlpha;
    // Might use mIconFullTextAlpha instead of mIconLastSetTextAlpha if we are translucent.
    private int mIconCurrentTextAlpha;

    private final PackageManager mPackageManager;
    private final String[] prefsToWatch = {"pref_iconShape", "pref_colorizeGeneratedBackgrounds",
            "pref_enableWhiteOnlyTreatment", "pref_enableLegacyTreatment",
            "pref_generateAdaptiveForIconPack", "pref_forceShapeless"};

    protected final OmegaPreferences prefs;

    public IconPreview(Context context) {
        this(context, null, 0);
    }

    public IconPreview(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconPreview(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.HORIZONTAL);
        wallpaper = WallpaperPreviewProvider.Companion.getInstance(context).getWallpaper();
        mPackageManager = context.getPackageManager();
        mLauncher = OmegaLauncher.getLauncher(context);
        prefs = Utilities.getOmegaPrefs(context);

        mIconTextColor = Themes.getAttrColor(context, android.R.attr.textColorSecondary);
        mIconFullTextAlpha = Color.alpha(mIconTextColor);
        mIconCurrentTextAlpha = mIconFullTextAlpha;
    }

    @Override
    public void onAppsUpdated() {
        removeAllViews();
        loadPreviewComponents();
    }

    public void loadPreviewComponents() {
        List<ComponentKey> list = new ArrayList<>();
        String[] components = CustomAppPredictor.PLACE_HOLDERS;

        for (String placeHolder : components) {
            Intent intent = mPackageManager.getLaunchIntentForPackage(placeHolder);
            if (intent != null) {
                ComponentName componentInfo = intent.getComponent();
                if (componentInfo != null) {
                    list.add(new ComponentKey(componentInfo, Process.myUserHandle()));
                }
            }
        }

        mPreviewAppComponents.clear();
        mPreviewAppComponents.addAll(list);

        mPreviewApps.clear();
        mPreviewApps.addAll(processPreviewAppComponents(mPreviewAppComponents));

        applyPreviewIcons();
    }

    private List<ItemInfoWithIcon> processPreviewAppComponents(List<ComponentKey> components) {
        if (getAppsStore().getApps().length < 1) {
            // Apps have not been bound yet.
            return Collections.emptyList();
        }

        List<ItemInfoWithIcon> previewApps = new ArrayList<>();
        for (ComponentKey mapper : components) {
            ItemInfoWithIcon info = getAppsStore().getApp(mapper);
            if (info != null) {
                previewApps.add(info);
            }
            if (previewApps.size() == 20) {
                break;
            }
        }
        return previewApps;
    }

    private void applyPreviewIcons() {
        for (int i = 0; i < 5; i++) {
            BubbleTextView icon = (BubbleTextView) mLauncher.getLayoutInflater().inflate(
                    R.layout.all_apps_icon, this, false);
            LayoutParams lp = (LayoutParams) icon.getLayoutParams();
            lp.height = mLauncher.getDeviceProfile().allAppsCellHeightPx;
            lp.width = 0;
            lp.weight = 1;
            addView(icon);
        }
        for (int i = 0; i < 5; i++) {
            BubbleTextView icon = (BubbleTextView) getChildAt(i);
            icon.reset();
            icon.setVisibility(View.VISIBLE);
            icon.applyFromApplicationInfo((AppInfo) mPreviewApps.get(i));
            icon.setTextColor(mIconTextColor);
        }
        mLauncher.reapplyUi();
        setVisibility(View.VISIBLE);
    }

    public void setTextAlpha(int textAlpha) {
        mIconLastSetTextAlpha = textAlpha;
        if (getAlpha() < 1 && textAlpha > 0) {
            // If the entire header is translucent, make sure the text is at full opacity so it's
            // not double-translucent. However, we support keeping the text invisible (alpha == 0).
            textAlpha = mIconFullTextAlpha;
        }
        mIconCurrentTextAlpha = textAlpha;
        int iconColor = setColorAlphaBound(mIconTextColor, mIconCurrentTextAlpha);
        for (int i = 0; i < getChildCount(); i++) {
            ((BubbleTextView) getChildAt(i)).setTextColor(iconColor);
        }
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        // Reapply text alpha so that we update it to be full alpha if the row is now translucent.
        setTextAlpha(mIconLastSetTextAlpha);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getAppsStore().registerIconContainer(this);
        prefs.addOnPreferenceChangeListener(this, prefsToWatch);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getAppsStore().unregisterIconContainer(this);
        prefs.reloadIcons();
        prefs.removeOnPreferenceChangeListener(this, prefsToWatch);
    }

    private AllAppsStore getAppsStore() {
        return mLauncher.getAppsView().getAppsStore();
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
    public void fillInLogContainerData(View v, ItemInfo info, Target target, Target targetParent) {
        target.gridX = info.cellX;
        target.gridY = info.cellY;
        target.pageIndex = 0;
        targetParent.containerType = ContainerType.WORKSPACE;
    }

    @Override
    public void onValueChanged(@NotNull String key, @NotNull OmegaPreferences prefs, boolean force) {
        removeAllViews();
        MAIN_EXECUTOR.execute(this::loadPreviewComponents);
        invalidate();
    }
}
