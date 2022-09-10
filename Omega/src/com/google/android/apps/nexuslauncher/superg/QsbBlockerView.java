package com.google.android.apps.nexuslauncher.superg;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.logging.StatsLogManager;
import com.android.launcher3.model.data.ItemInfo;
import com.saggitt.omega.OmegaAppKt;
import com.saggitt.omega.preferences.OmegaPreferences;
import com.saggitt.omega.smartspace.OmegaSmartSpaceController;
import com.saggitt.omega.smartspace.OmegaSmartSpaceController.WeatherData;
import com.saggitt.omega.smartspace.SmartSpacePreferencesShortcut;
import com.saggitt.omega.util.ContextExtensionsKt;
import com.saggitt.omega.util.OmegaUtilsKt;
import com.saggitt.omega.util.Temperature;

import org.jetbrains.annotations.Nullable;

/**
 * A simple view used to show the region blocked by QSB during drag and drop.
 */
public class QsbBlockerView extends FrameLayout implements OmegaSmartSpaceController.Listener,
        OnClickListener, OnLongClickListener {
    private final OmegaSmartSpaceController mController;
    private int mState = 0;
    private View mView;
    private BubbleTextView mDummyBubbleTextView;

    private final Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public QsbBlockerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBgPaint.setColor(Color.WHITE);
        mBgPaint.setAlpha(0);

        mController = OmegaAppKt.getOmegaApp(getContext()).getSmartspace();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        BubbleTextView mDummyBubbleTextView = findViewById(R.id.dummyBubbleTextView);
        mDummyBubbleTextView.setTag(new ItemInfo() {
            @Override
            public ComponentName getTargetComponent() {
                return new ComponentName(getContext(), "");
            }
        });
        mDummyBubbleTextView.setContentDescription("");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mView != null && mState == 2) {
            Launcher launcher = ContextExtensionsKt.getLauncherOrNull(getContext());
            int size;
            if (launcher != null) {
                DeviceProfile deviceProfile = launcher.getDeviceProfile();
                if (launcher.useVerticalBarLayout()) {
                    size = ((MeasureSpec.getSize(widthMeasureSpec) / deviceProfile.inv.numColumns)
                            - deviceProfile.iconSizePx) / 2;
                } else {
                    size = 0;
                }
            } else {
                size = getResources().getDimensionPixelSize(R.dimen.smartspace_preview_widget_margin);
            }
            LayoutParams layoutParams = (LayoutParams) mView.getLayoutParams();
            layoutParams.leftMargin = layoutParams.rightMargin = size;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mController != null)
            mController.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mController != null)
            mController.removeListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
    }

    @Override
    public void onDataUpdated(@Nullable WeatherData weather, @Nullable OmegaSmartSpaceController.CardData card) {
        final int oldState = mState;
        final View oldView = mView;

        if (!Utilities.getOmegaPrefs(getContext()).getSmartspaceUsePillQsb().onGetValue()) {
            return;
        }

        if (weather == null) {
            mState = 1;
            mView = oldView != null && oldState == 1 ?
                    oldView :
                    LayoutInflater.from(getContext()).inflate(R.layout.date_widget, this, false);
        } else {
            mState = 2;
            mView = oldView != null && oldState == 2 ?
                    oldView :
                    LayoutInflater.from(getContext()).inflate(R.layout.weather_widget, this, false);
            applyWeather(mView, weather);
            mView.setOnClickListener(this);
        }

        if (oldState != mState) {
            if (oldView != null) {
                oldView.animate().setDuration(200L).alpha(0f).withEndAction(
                        () -> removeView(oldView));
            }
            addView(mView);
            mView.setAlpha(0f);
            mView.animate().setDuration(200L).alpha(1f);
        } else if (oldView != mView) {
            if (oldView != null) {
                removeView(oldView);
            }
            addView(mView);
        }

        mView.setOnLongClickListener(this);
    }

    private void applyWeather(View view, WeatherData weather) {
        ImageView weatherIcon = view.findViewById(R.id.weather_widget_icon);
        weatherIcon.setImageBitmap(weather.getIcon());
        TextView weatherTemperature = view.findViewById(R.id.weather_widget_temperature);
        OmegaPreferences prefs = Utilities.getOmegaPrefs(getContext());
        String currentUnit = prefs.getSmartspaceWeatherUnit().onGetValue();
        weatherTemperature.setText(weather.getTitle(Temperature.Companion.unitFromString(currentUnit)));
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    public void onClick(View v) {
        if (mController != null)
            mController.openWeather(v);
    }

    @Override
    public boolean onLongClick(View v) {
        OmegaUtilsKt.openPopupMenu(mView, null,
                new SmartSpacePreferencesShortcut(getContext(), StatsLogManager.LauncherRankingEvent.UNKNOWN));
        return false;
    }
}