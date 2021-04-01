package com.google.android.apps.nexuslauncher.qsb;

import static com.google.android.apps.nexuslauncher.search.nano.SearchProto.SearchView;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsRecyclerView;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.icons.BitmapRenderer;
import com.android.launcher3.pm.UserCache;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.google.android.apps.nexuslauncher.search.nano.SearchProto.AppIndex;
import com.google.android.apps.nexuslauncher.search.nano.SearchProto.Columns;
import com.google.android.apps.nexuslauncher.search.nano.SearchProto.SearchBase;
import com.google.protobuf.nano.MessageNano;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.util.Config;

import java.util.ArrayList;

public class ConfigBuilder {
    private final SearchBase mNano;
    private final OmegaLauncher mActivity;
    private final Bundle mBundle;
    private final AbstractQsbLayout mQsbLayout;
    private final boolean mIsAllApps;
    private final UserCache userCache;
    private boolean co;
    private BubbleTextView mBubbleTextView;

    public ConfigBuilder(AbstractQsbLayout qsbLayout, boolean isAllApps) {
        mBundle = new Bundle();
        mNano = new SearchBase();
        mQsbLayout = qsbLayout;
        mActivity = qsbLayout.getLauncher();
        mIsAllApps = isAllApps;
        userCache = UserCache.INSTANCE.get(mActivity);
    }

    public static Intent getSearchIntent(Rect sourceBounds, View gIcon, View micIcon) {
        Intent intent = new Intent("com.google.nexuslauncher.FAST_TEXT_SEARCH");
        intent.setSourceBounds(sourceBounds);
        if (micIcon.getVisibility() != View.VISIBLE) {
            intent.putExtra("source_mic_alpha", 0f);
        }
        return intent.putExtra("source_round_left", true)
                .putExtra("source_round_right", true)
                .putExtra("source_logo_offset", getCenter(gIcon, sourceBounds))
                .putExtra("source_mic_offset", getCenter(micIcon, sourceBounds))
                .putExtra("use_fade_animation", true)
                .setPackage(Config.GOOGLE_QSB)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private static Point getCenter(final View view, final Rect rect) {
        final int[] location = new int[2];
        view.getLocationInWindow(location);
        final Point point = new Point();
        point.x = location[0] - rect.left + view.getWidth() / 2;
        point.y = location[1] - rect.top + view.getHeight() / 2;
        return point;
    }

    private static Columns getViewBounds(final View view) {
        final Columns a = new Columns();
        a.iconDistance = view.getWidth();
        a.height = view.getHeight();
        final int[] array = new int[2];
        view.getLocationInWindow(array);
        a.edgeMargin = array[0];
        a.innerMargin = array[1];
        return a;
    }

    private void updateHotseatSearchDimens() {
        if (mNano.appsView != null) {
            return;
        }
        final Columns apps = mNano.apps;
        final Columns appsView = new Columns();
        appsView.edgeMargin = apps.edgeMargin;
        appsView.innerMargin = apps.innerMargin + apps.height;
        appsView.height = apps.height;
        appsView.iconDistance = apps.iconDistance;
        mNano.appsView = appsView;
    }

    private AllAppsRecyclerView getAppsView() {
        return (AllAppsRecyclerView) mActivity.findViewById(R.id.apps_list_view);
    }

    private int getBackgroundColor() {
        return ColorUtils.compositeColors(Themes.getAttrColor(mActivity, R.attr.allAppsScrimColor),
                ColorUtils.setAlphaComponent(WallpaperColorInfo.getInstance(mActivity).getMainColor(), 255));
    }

    private RemoteViews searchIconTemplate() {
        final RemoteViews remoteViews = new RemoteViews(mActivity.getPackageName(), R.layout.apps_search_icon_template);

        final int iconSize = mBubbleTextView.getIconSize();
        final int horizontalPadding = (mBubbleTextView.getWidth() - iconSize) / 2;
        final int paddingTop = mBubbleTextView.getPaddingTop();
        final int paddingBottom = mBubbleTextView.getHeight() - iconSize - paddingTop;
        remoteViews.setViewPadding(android.R.id.icon, horizontalPadding, paddingTop, horizontalPadding, paddingBottom);
        final int minPadding = Math.min((int) (iconSize * 0.12f), Math.min(horizontalPadding, Math.min(paddingTop, paddingBottom)));
        remoteViews.setViewPadding(R.id.click_feedback_wrapper, horizontalPadding - minPadding, paddingTop - minPadding, horizontalPadding - minPadding, paddingBottom - minPadding);
        remoteViews.setTextViewTextSize(android.R.id.title, 0, mActivity.getDeviceProfile().allAppsIconTextSizePx);
        remoteViews.setViewPadding(android.R.id.title, mBubbleTextView.getPaddingLeft(), mBubbleTextView.getCompoundDrawablePadding() + mBubbleTextView.getIconSize(), mBubbleTextView.getPaddingRight(), 0);

        return remoteViews;
    }

    private RemoteViews searchQsbTemplate() {
        final RemoteViews remoteViews = new RemoteViews(mActivity.getPackageName(), R.layout.apps_search_qsb_template);

        final int effectiveHeight = mQsbLayout.getHeight() - mQsbLayout.getPaddingTop() - mQsbLayout.getPaddingBottom() + 20;
        final Bitmap mShadowBitmap = mQsbLayout.mAllAppsShadowBitmap;
        if (mShadowBitmap != null) {
            final int internalWidth = (mShadowBitmap.getWidth() - effectiveHeight) / 2;
            final int verticalPadding = (mQsbLayout.getHeight() - mShadowBitmap.getHeight()) / 2;
            remoteViews.setViewPadding(R.id.qsb_background_container, mQsbLayout.getPaddingLeft() - internalWidth, verticalPadding, mQsbLayout.getPaddingRight() - internalWidth, verticalPadding);
            final Bitmap bitmap = Bitmap.createBitmap(mShadowBitmap, 0, 0, mShadowBitmap.getWidth() / 2, mShadowBitmap.getHeight());
            final Bitmap bitmap2 = Bitmap.createBitmap(mShadowBitmap, (mShadowBitmap.getWidth() - 20) / 2, 0, 20, mShadowBitmap.getHeight());
            remoteViews.setImageViewBitmap(R.id.qsb_background_1, bitmap);
            remoteViews.setImageViewBitmap(R.id.qsb_background_2, bitmap2);
            remoteViews.setImageViewBitmap(R.id.qsb_background_3, bitmap);
        }
        if (mQsbLayout.mMicIconView.getVisibility() != View.VISIBLE) {
            remoteViews.setViewVisibility(R.id.mic_icon, View.INVISIBLE);
        }

        final View gIcon = mQsbLayout.findViewById(R.id.g_icon);
        int horizontalPadding = mQsbLayout.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ?
                mQsbLayout.getWidth() - gIcon.getRight() :
                gIcon.getLeft();
        remoteViews.setViewPadding(R.id.qsb_icon_container, horizontalPadding, 0, horizontalPadding, 0);

        return remoteViews;
    }

    private void createSearchTemplate() {
        mNano.searchTemplate = "search_box_template";
        mBundle.putParcelable(mNano.searchTemplate, searchQsbTemplate());
        mNano.gIcon = R.id.g_icon;
        mNano.micIcon = mQsbLayout.mMicIconView.getVisibility() == View.VISIBLE ?
                R.id.mic_icon :
                0;
        final Columns viewBounds = getViewBounds(mActivity.getDragLayer());
        final int topShift = mNano.apps.innerMargin + (co ? 0 : mNano.apps.height);
        viewBounds.innerMargin += topShift;
        viewBounds.height -= topShift;
        mNano.viewBounds = viewBounds;
        if (viewBounds.iconDistance > 0 && viewBounds.height > 0) {
            Bitmap bitmap = BitmapRenderer.createHardwareBitmap(viewBounds.iconDistance, viewBounds.height, out -> a(topShift, out));
            mBundle.putParcelable(mNano.view, bitmap);
        } else {
            String stringBuilder = "Invalid preview bitmap size. width: " +
                    viewBounds.iconDistance +
                    "hight: " +
                    viewBounds.height +
                    " top shift: " +
                    topShift;
            Log.e("ConfigBuilder", stringBuilder);
            viewBounds.height = 0;
            viewBounds.edgeMargin = 0;
            viewBounds.innerMargin = 0;
            viewBounds.iconDistance = 0;
            Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            bitmap.setPixel(0, 0, 0);
            mBundle.putParcelable(mNano.view, bitmap);
        }
    }

    private void a(int i, Canvas canvas) {
        int save = canvas.save();
        canvas.translate(0.0f, (float) (-i));
        a(canvas, mActivity.getAppsView().getRecyclerViewContainer());
        a(canvas, mActivity.getAppsView().getFloatingHeaderView());
        canvas.restoreToCount(save);
    }

    private void a(Canvas canvas, View view) {
        final int[] array = {0, 0};
        mActivity.getDragLayer().mapCoordInSelfToDescendant(mActivity.getAppsView(), array);
        mActivity.getDragLayer().mapCoordInSelfToDescendant(view, array);
        canvas.translate((float) (-array[0]), (float) (-array[1]));
        view.draw(canvas);
        canvas.translate((float) array[0], (float) array[1]);
    }

    private void setAllAppsSearchView() {
        View view = null;
        AllAppsRecyclerView appsView = getAppsView();
        GridLayoutManager.SpanSizeLookup spanSizeLookup = ((GridLayoutManager) appsView.getLayoutManager())
                .getSpanSizeLookup();
        int allAppsCols = Math.min(mActivity.getDeviceProfile().inv.numColsDrawer, appsView.getChildCount());
        int childCount = appsView.getChildCount();
        BubbleTextView[] bubbleTextViewArr = new BubbleTextView[allAppsCols];
        int i4 = -1;
        for (int i = 0; i < childCount; i++) {
            RecyclerView.ViewHolder childViewHolder = appsView.getChildViewHolder(appsView.getChildAt(i));
            if (childViewHolder.itemView instanceof BubbleTextView) {
                int spanGroupIndex = spanSizeLookup.getSpanGroupIndex(childViewHolder.getLayoutPosition(), allAppsCols);
                if (spanGroupIndex >= 0) {
                    if (i4 >= 0) {
                        if (spanGroupIndex != i4) {
                            view = childViewHolder.itemView;
                            break;
                        }
                    }
                    i4 = spanGroupIndex;
                    int index = ((GridLayoutManager.LayoutParams) childViewHolder.itemView.getLayoutParams()).getSpanIndex();
                    bubbleTextViewArr[index] = (BubbleTextView) childViewHolder.itemView;
                }
            }
        }
        if (bubbleTextViewArr.length == 0 || bubbleTextViewArr[0] == null) {
            Log.e("ConfigBuilder", "No icons rendered in all apps");
            setHotseatSearchView();
            return;
        }
        mBubbleTextView = bubbleTextViewArr[0];
        mNano.allAppsCols = allAppsCols;
        int iconCountOffset = 0;
        for (int i = 0; i < bubbleTextViewArr.length; i++) {
            if (bubbleTextViewArr[i] == null) {
                iconCountOffset = allAppsCols - i;
                allAppsCols = i;
                break;
            }
        }
        co = appsView.getChildViewHolder(bubbleTextViewArr[0]).getItemViewType() == 4;
        Columns lastColumn = getViewBounds(bubbleTextViewArr[allAppsCols - 1]);
        Columns firstColumn = getViewBounds(bubbleTextViewArr[0]);
        if (Utilities.isRtl(mActivity.getResources())) {
            Columns temp = lastColumn;
            lastColumn = firstColumn;
            firstColumn = temp;
        }
        int iconWidth = lastColumn.iconDistance;
        int totalIconDistance = lastColumn.edgeMargin - firstColumn.edgeMargin;
        int iconDistance = totalIconDistance / allAppsCols;
        firstColumn.iconDistance = iconWidth + totalIconDistance;
        if (Utilities.isRtl(mActivity.getResources())) {
            firstColumn.edgeMargin -= iconCountOffset * iconWidth;
            firstColumn.iconDistance += iconCountOffset * iconWidth;
        } else {
            firstColumn.iconDistance += iconCountOffset * (iconDistance + iconWidth);
        }
        mNano.apps = firstColumn;
        if (!this.co) {
            firstColumn.innerMargin -= firstColumn.height;
        } else if (view != null) {
            Columns viewBounds3 = getViewBounds(view);
            viewBounds3.iconDistance = firstColumn.iconDistance;
            mNano.appsView = viewBounds3;
        }
        updateHotseatSearchDimens();
    }

    private void setHotseatSearchView() {
        mNano.allAppsCols = mActivity.getDeviceProfile().inv.numColumns;
        final int width = mActivity.getHotseat().getWidth();
        final int dimensionPixelSize = mActivity.getResources().getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        final Columns appCol = new Columns();
        appCol.edgeMargin = dimensionPixelSize;
        appCol.iconDistance = width - dimensionPixelSize - dimensionPixelSize;
        appCol.height = mActivity.getDeviceProfile().allAppsCellHeightPx;
        mNano.apps = appCol;
        updateHotseatSearchDimens();
        AlphabeticalAppsList apps = getAppsView().getApps();
        mBubbleTextView = (BubbleTextView) mActivity.getLayoutInflater().inflate(R.layout.all_apps_icon, getAppsView(), false);
        ViewGroup.LayoutParams layoutParams = mBubbleTextView.getLayoutParams();
        layoutParams.height = appCol.height;
        layoutParams.width = appCol.iconDistance / mNano.allAppsCols;
        if (!apps.getApps().isEmpty()) {
            mBubbleTextView.applyFromApplicationInfo(apps.getApps().get(0));
        }
        mBubbleTextView.measure(View.MeasureSpec.makeMeasureSpec(layoutParams.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY));
        mBubbleTextView.layout(0, 0, layoutParams.width, layoutParams.height);
        final ArrayList<AppIndex> list = new ArrayList<>(mNano.allAppsCols);
        mNano.index = list.toArray(new AppIndex[0]);
    }

    public byte[] build() {
        mNano.bgColor = getBackgroundColor();
        mNano.isDark = Themes.getAttrBoolean(mActivity, R.attr.isMainColorDark);
        if (mIsAllApps) {
            setAllAppsSearchView();
        } else {
            setHotseatSearchView();
        }
        mNano.iconViewTemplate = "icon_view_template";
        mBundle.putParcelable(mNano.iconViewTemplate, searchIconTemplate());
        mNano.iconLongClick = "icon_long_click";
        mBundle.putParcelable(mNano.iconLongClick, PendingIntent
                .getBroadcast(mActivity, 2055, new Intent()
                                .setComponent(new ComponentName(mActivity, LongClickReceiver.class)),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT));
        LongClickReceiver.bq(mActivity);
        mNano.bounds = getViewBounds(mQsbLayout);
        mNano.isAllApps = mIsAllApps;
        if (mIsAllApps) {
            createSearchTemplate();
        }
        SearchView searchView = new SearchView();
        searchView.base = mNano;
        return MessageNano.toByteArray(searchView);
    }

    public Bundle getExtras() {
        return mBundle;
    }
}
