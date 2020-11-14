package com.saggitt.omega.icons.clock;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.icons.LauncherIcons;
import com.android.launcher3.util.Preconditions;

import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;
import java.util.WeakHashMap;

@TargetApi(26)
public class CustomClock {
    private final Context mContext;
    private final Set<AutoUpdateClock> mUpdaters = Collections.newSetFromMap(new WeakHashMap<>());

    public CustomClock(Context context) {
        mContext = context;

        mContext.registerReceiver(new BroadcastReceiver() {
                                      @Override
                                      public void onReceive(Context context, Intent intent) {
                                          loadTimeZone(intent.getStringExtra("time-zone"));
                                      }
                                  }, new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED), null,
                new Handler(Looper.getMainLooper()));
    }

    public static Drawable getClock(Context context, Drawable drawable, Metadata metadata) {
        ClockLayers clone = getClockLayers(context, drawable, metadata, false).clone();
        clone.updateAngles();
        return clone.mDrawable;
    }

    private static ClockLayers getClockLayers(Context context, Drawable drawableForDensity,
                                              Metadata metadata, boolean normalizeIcon) {
        Preconditions.assertWorkerThread();
        ClockLayers layers = new ClockLayers();
        layers.setDrawable(drawableForDensity.mutate());
        layers.mHourIndex = metadata.HOUR_LAYER_INDEX;
        layers.mMinuteIndex = metadata.MINUTE_LAYER_INDEX;
        layers.mSecondIndex = metadata.SECOND_LAYER_INDEX;
        layers.mDefaultHour = metadata.DEFAULT_HOUR;
        layers.mDefaultMinute = metadata.DEFAULT_MINUTE;
        layers.mDefaultSecond = metadata.DEFAULT_SECOND;
        if (normalizeIcon) {
            LauncherIcons obtain = LauncherIcons.obtain(context);
            float[] scale = new float[1];
            layers.bitmap = obtain.createBadgedIconBitmap(
                    new AdaptiveIconDrawable(
                            layers.mDrawable.getBackground().getConstantState().newDrawable(),
                            null),
                    Process.myUserHandle(), Build.VERSION_CODES.O, false, scale).icon;
            layers.scale = scale[0];

            int iconBitmapSize = LauncherAppState.getInstance(context)
                    .getInvariantDeviceProfile().iconBitmapSize;
            layers.offset = (int) Math.ceil((double) (0.010416667f * ((float) iconBitmapSize)));
            obtain.recycle();
        }

        LayerDrawable layerDrawable = layers.mLayerDrawable;
        int numberOfLayers = layerDrawable.getNumberOfLayers();

        if (layers.mHourIndex < 0 || layers.mHourIndex >= numberOfLayers) {
            layers.mHourIndex = -1;
        }
        if (layers.mMinuteIndex < 0 || layers.mMinuteIndex >= numberOfLayers) {
            layers.mMinuteIndex = -1;
        }
        if (layers.mSecondIndex < 0 || layers.mSecondIndex >= numberOfLayers) {
            layers.mSecondIndex = -1;
        }

        return layers;
    }

    public FastBitmapDrawable drawIcon(ItemInfoWithIcon info, Drawable drawableForDensity,
                                       Metadata metadata) {
        final AutoUpdateClock updater = new AutoUpdateClock(info.iconBitmap,
                getClockLayers(mContext, drawableForDensity, metadata, true).clone()
        );
        mUpdaters.add(updater);
        return updater;
    }

    private void loadTimeZone(String timeZoneId) {
        TimeZone timeZone = timeZoneId == null ?
                TimeZone.getDefault() :
                TimeZone.getTimeZone(timeZoneId);

        for (AutoUpdateClock a : mUpdaters) {
            a.setTimeZone(timeZone);
        }
    }

    public static class Metadata {
        final int HOUR_LAYER_INDEX;
        final int MINUTE_LAYER_INDEX;
        final int SECOND_LAYER_INDEX;

        final int DEFAULT_HOUR;
        final int DEFAULT_MINUTE;
        final int DEFAULT_SECOND;

        public Metadata(int hourIndex, int minuteIndex, int secondIndex,
                        int defaultHour, int defaultMinute, int defaultSecond) {
            HOUR_LAYER_INDEX = hourIndex;
            MINUTE_LAYER_INDEX = minuteIndex;
            SECOND_LAYER_INDEX = secondIndex;
            DEFAULT_HOUR = defaultHour;
            DEFAULT_MINUTE = defaultMinute;
            DEFAULT_SECOND = defaultSecond;
        }
    }
}
