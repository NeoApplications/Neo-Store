package com.android.launcher3.popup;

import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_SYSTEM_SHORTCUT_APP_INFO_TAP;
import static com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_SYSTEM_SHORTCUT_WIDGETS_TAP;
import static com.android.launcher3.model.data.ItemInfoWithIcon.FLAG_SYSTEM_YES;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Rect;
import android.net.Uri;
import android.os.UserHandle;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.model.WidgetItem;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.ItemInfoWithIcon;
import com.android.launcher3.model.data.LauncherAppWidgetInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.userevent.nano.LauncherLogProto.Action;
import com.android.launcher3.userevent.nano.LauncherLogProto.ControlType;
import com.android.launcher3.util.InstantAppResolver;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.widget.WidgetsBottomSheet;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.override.CustomInfoProvider;
import com.saggitt.omega.util.OmegaUtilsKt;
import com.saggitt.omega.views.CustomBottomSheet;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Represents a system shortcut for a given app. The shortcut should have a label and icon, and an
 * onClickListener that depends on the item that the shortcut services.
 * <p>
 * Example system shortcuts, defined as inner classes, include Widgets and AppInfo.
 *
 * @param <T>
 */
public abstract class SystemShortcut<T extends BaseDraggingActivity> extends ItemInfo
        implements View.OnClickListener {

    private final int mIconResId;
    private final int mLabelResId;
    private final int mAccessibilityActionId;

    public static final Factory<Launcher> WIDGETS = (launcher, itemInfo) -> {
        if (itemInfo.getTargetComponent() == null) return null;
        final List<WidgetItem> widgets =
                launcher.getPopupDataProvider().getWidgetsForPackageUser(new PackageUserKey(
                        itemInfo.getTargetComponent().getPackageName(), itemInfo.user));
        if (widgets == null) {
            return null;
        }
        return new Widgets(launcher, itemInfo);
    };
    public static final Factory<BaseDraggingActivity> APP_INFO = AppInfo::new;
    public static final Factory<BaseDraggingActivity> INSTALL = (activity, itemInfo) -> {
        boolean supportsWebUI = (itemInfo instanceof WorkspaceItemInfo)
                && ((WorkspaceItemInfo) itemInfo).hasStatusFlag(
                WorkspaceItemInfo.FLAG_SUPPORTS_WEB_UI);
        boolean isInstantApp = false;
        if (itemInfo instanceof com.android.launcher3.model.data.AppInfo) {
            com.android.launcher3.model.data.AppInfo
                    appInfo = (com.android.launcher3.model.data.AppInfo) itemInfo;
            isInstantApp = InstantAppResolver.newInstance(activity).isInstantApp(appInfo);
        }
        boolean enabled = supportsWebUI || isInstantApp;
        if (!enabled) {
            return null;
        }
        return new Install(activity, itemInfo);
    };
    public static final Factory<Launcher> APP_EDIT = (launcher, itemInfo) -> {
        OmegaPreferences prefs = Utilities.getOmegaPrefs(launcher);
        AppEdit appEdit = null;

        if (Launcher.getLauncher(launcher).isInState(ALL_APPS)) {
            if (prefs.getDrawerPopupEdit()) {
                appEdit = new AppEdit(launcher, itemInfo);
            }
        } else if (Launcher.getLauncher(launcher).isInState(NORMAL)) {
            if (prefs.getDesktopPopupEdit()
                    && !prefs.getLockDesktop()
                    && CustomInfoProvider.Companion.isEditable(itemInfo)) {
                appEdit = new AppEdit(launcher, itemInfo);
            }
        }
        return appEdit;
    };

    public static final Factory<Launcher> APP_REMOVE = (launcher, itemInfo) -> {
        OmegaPreferences prefs = Utilities.getOmegaPrefs(launcher);
        AppRemove appRemove = null;
        if (Launcher.getLauncher(launcher).isInState(NORMAL)) {
            if (itemInfo instanceof WorkspaceItemInfo
                    || itemInfo instanceof LauncherAppWidgetInfo
                    || itemInfo instanceof FolderInfo) {
                if (prefs.getDesktopPopupRemove()
                        && !prefs.getLockDesktop()
                        && CustomInfoProvider.Companion.isEditable(itemInfo)) {
                    appRemove = new AppRemove(launcher, itemInfo);
                }
            }
        }
        return appRemove;
    };
    public static final Factory<Launcher> APP_UNINSTALL = (launcher, itemInfo) -> {
        OmegaPreferences prefs = Utilities.getOmegaPrefs(launcher);
        AppUninstall appUninstall = null;

        ;

        if (prefs.getDrawerPopupUninstall() || Launcher.getLauncher(launcher).isInState(ALL_APPS)) {
            if (itemInfo instanceof ItemInfoWithIcon
                    && OmegaUtilsKt.hasFlag(((ItemInfoWithIcon) itemInfo).runtimeStatusFlags, FLAG_SYSTEM_YES)) {
                appUninstall = null;
            } else {
                appUninstall = new AppUninstall(launcher, itemInfo);
            }
        }
        return appUninstall;
    };

    protected final T mTarget;

    /**
     * Should be in the left group of icons in app's context menu header.
     */
    public boolean isLeftGroup() {
        return false;
    }

    protected final ItemInfo mItemInfo;

    public SystemShortcut(int iconResId, int labelResId, T target, ItemInfo itemInfo) {
        mIconResId = iconResId;
        mLabelResId = labelResId;
        mAccessibilityActionId = labelResId;
        mTarget = target;
        mItemInfo = itemInfo;
    }

    public SystemShortcut(SystemShortcut<T> other) {
        mIconResId = other.mIconResId;
        mLabelResId = other.mLabelResId;
        mAccessibilityActionId = other.mAccessibilityActionId;
        mTarget = other.mTarget;
        mItemInfo = other.mItemInfo;
    }

    public boolean hasHandlerForAction(int action) {
        return mAccessibilityActionId == action;
    }

    public static void dismissTaskMenuView(BaseDraggingActivity activity) {
        AbstractFloatingView.closeOpenViews(activity, true,
                AbstractFloatingView.TYPE_ALL & ~AbstractFloatingView.TYPE_REBIND_SAFE);
    }

    public void setIconAndLabelFor(View iconView, TextView labelView) {
        iconView.setBackgroundResource(mIconResId);
        labelView.setText(mLabelResId);
    }

    public void setIconAndContentDescriptionFor(ImageView view) {
        view.setImageResource(mIconResId);
        view.setContentDescription(view.getContext().getText(mLabelResId));
    }

    public AccessibilityNodeInfo.AccessibilityAction createAccessibilityAction(Context context) {
        return new AccessibilityNodeInfo.AccessibilityAction(
                mAccessibilityActionId, context.getText(mLabelResId));
    }

    public interface Factory<T extends BaseDraggingActivity> {

        @Nullable
        SystemShortcut<T> getShortcut(T activity, ItemInfo itemInfo);
    }

    public static class Widgets extends SystemShortcut<Launcher> {
        public Widgets(Launcher target, ItemInfo itemInfo) {
            super(R.drawable.ic_widget, R.string.widget_button_text, target, itemInfo);
        }

        @Override
        public void onClick(View view) {
            AbstractFloatingView.closeAllOpenViews(mTarget);
            WidgetsBottomSheet widgetsBottomSheet =
                    (WidgetsBottomSheet) mTarget.getLayoutInflater().inflate(
                            R.layout.widgets_bottom_sheet, mTarget.getDragLayer(), false);
            widgetsBottomSheet.populateAndShow(mItemInfo);
            mTarget.getUserEventDispatcher().logActionOnControl(Action.Touch.TAP,
                    ControlType.WIDGETS_BUTTON, view);
            mTarget.getStatsLogManager().logger().withItemInfo(mItemInfo)
                    .log(LAUNCHER_SYSTEM_SHORTCUT_WIDGETS_TAP);
        }
    }

    public static class AppInfo extends SystemShortcut {

        public AppInfo(BaseDraggingActivity target, ItemInfo itemInfo) {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label, target,
                    itemInfo);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            Rect sourceBounds = mTarget.getViewBounds(view);
            new PackageManagerHelper(mTarget).startDetailsActivityForInfo(
                    mItemInfo, sourceBounds, ActivityOptions.makeBasic().toBundle());
            mTarget.getUserEventDispatcher().logActionOnControl(Action.Touch.TAP,
                    ControlType.APPINFO_TARGET, view);
            mTarget.getStatsLogManager().logger().withItemInfo(mItemInfo)
                    .log(LAUNCHER_SYSTEM_SHORTCUT_APP_INFO_TAP);
        }
    }

    public static class Install extends SystemShortcut {

        public Install(BaseDraggingActivity target, ItemInfo itemInfo) {
            super(R.drawable.ic_install_no_shadow, R.string.install_drop_target_label,
                    target, itemInfo);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new PackageManagerHelper(view.getContext()).getMarketIntent(
                    mItemInfo.getTargetComponent().getPackageName());
            mTarget.startActivitySafely(view, intent, mItemInfo, null);
            AbstractFloatingView.closeAllOpenViews(mTarget);
        }
    }

    public static class AppEdit extends SystemShortcut {
        public AppEdit(Launcher target, ItemInfo itemInfo) {
            super(R.drawable.ic_edit_no_shadow, R.string.action_preferences, target, itemInfo);
        }

        public void onClick(View v) {
            AbstractFloatingView.closeAllOpenViews(mTarget);
            CustomBottomSheet.show((Launcher) mTarget, mItemInfo);
        }
    }

    public static class AppRemove extends SystemShortcut {
        Launcher mLauncher;

        public AppRemove(Launcher target, ItemInfo itemInfo) {
            super(R.drawable.ic_remove_no_shadow, R.string.remove_drop_target_label, target, itemInfo);
            mLauncher = target;
        }

        public void onClick(View v) {
            AbstractFloatingView.closeAllOpenViews(mTarget);
            mLauncher.removeItem(null, mItemInfo, true);
            mLauncher.getModel().forceReload();
            mLauncher.getWorkspace().stripEmptyScreens();
        }
    }

    public static class AppUninstall extends SystemShortcut {
        Launcher mLauncher;

        public AppUninstall(Launcher target, ItemInfo itemInfo) {
            super(R.drawable.ic_uninstall_no_shadow, R.string.uninstall_drop_target_label, target, itemInfo);
            mLauncher = target;
        }

        public void onClick(View v) {
            AbstractFloatingView.closeAllOpenViews(mTarget);
            try {
                ComponentName componentName = getUninstallTarget((Launcher) mTarget, mItemInfo);
                Intent i = Intent.parseUri(mTarget.getString(R.string.delete_package_intent), 0)
                        .setData(Uri.fromParts("package", componentName.getPackageName(), componentName.getClassName()))
                        .putExtra(Intent.EXTRA_USER, mItemInfo.user);
                mTarget.startActivity(i);
            } catch (URISyntaxException e) {
                Toast.makeText(mLauncher, R.string.uninstall_failed, Toast.LENGTH_SHORT).show();
            }
        }

        private ComponentName getUninstallTarget(Launcher launcher, ItemInfo item) {
            if (item.itemType == ITEM_TYPE_APPLICATION && item.id == ItemInfo.NO_ID) {
                Intent intent = item.getIntent();
                UserHandle user = item.user;
                if (intent != null) {
                    LauncherActivityInfo info = launcher
                            .getSystemService(LauncherApps.class)
                            .resolveActivity(intent, user);
                    if (info != null && !(OmegaUtilsKt.hasFlag(info.getApplicationInfo().flags, ApplicationInfo.FLAG_SYSTEM))) {
                        return info.getComponentName();
                    }
                }
            } else {
                return item.getTargetComponent();
            }
            return null;
        }
    }
}
