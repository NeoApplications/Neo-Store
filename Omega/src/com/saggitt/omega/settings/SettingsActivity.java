/*
 *  Copyright (c) 2020 Omega Launcher
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
 *
 */

package com.saggitt.omega.settings;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceRecyclerViewAccessibilityDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.LauncherFiles;
import com.android.launcher3.R;
import com.android.launcher3.SessionCommitReceiver;
import com.android.launcher3.Utilities;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.saggitt.omega.FakeLauncherKt;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.preferences.ColorPreferenceCompat;
import com.saggitt.omega.preferences.ControlledPreference;
import com.saggitt.omega.preferences.PreferenceController;
import com.saggitt.omega.preferences.SubPreference;
import com.saggitt.omega.settings.search.SettingsSearchActivity;
import com.saggitt.omega.theme.ThemeOverride;
import com.saggitt.omega.util.SettingsObserver;
import com.saggitt.omega.views.SpringRecyclerView;
import com.saggitt.omega.views.ThemedListPreferenceDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SettingsActivity extends SettingsBaseActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        PreferenceFragment.OnPreferenceDisplayDialogCallback, View.OnClickListener, FragmentManager.OnBackStackChangedListener {

    public static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";

    public final static String EXTRA_TITLE = "title";
    public final static String EXTRA_FRAGMENT = "fragment";
    public final static String EXTRA_FRAGMENT_ARGS = "fragmentArgs";
    private boolean isSubSettings;
    protected boolean forceSubSettings = false;
    private boolean hasPreview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        savedInstanceState = getRelaunchInstanceState(savedInstanceState);

        String fragmentName = getIntent().getStringExtra(EXTRA_FRAGMENT);
        int content = getIntent().getIntExtra(SubSettingsFragment.CONTENT_RES_ID, 0);
        isSubSettings = content != 0 || fragmentName != null || forceSubSettings;
        hasPreview = getIntent().getBooleanExtra(SubSettingsFragment.HAS_PREVIEW, false);

        boolean showSearch = shouldShowSearch();

        super.onCreate(savedInstanceState);

        getDecorLayout().setHideToolbar(showSearch);
        getDecorLayout().setUseLargeTitle(shouldUseLargeTitle());
        setContentView(showSearch ? R.layout.activity_settings_home : R.layout.activity_settings);

        if (savedInstanceState == null) {
            Fragment fragment = createLaunchFragment(getIntent());

            // Display the fragment as the main content.
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        updateUpButton();

        if (showSearch) {
            Toolbar toolbar = findViewById(R.id.search_action_bar);
            toolbar.setOnClickListener(this);
        }

        if (hasPreview) {
            overrideOpenAnim();
        }
    }

    private void updateUpButton() {
        updateUpButton(isSubSettings || getSupportFragmentManager().getBackStackEntryCount() != 0);
    }

    private void updateUpButton(boolean enabled) {
        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
    }

    protected boolean shouldUseLargeTitle() {
        return !isSubSettings;
    }

    @Override
    public void onBackStackChanged() {
        updateUpButton();
    }

    protected boolean shouldShowSearch() {
        return BuildConfig.FEATURE_SETTINGS_SEARCH && !isSubSettings;
    }

    @NotNull
    @Override
    protected ThemeOverride.ThemeSet getThemeSet() {
        /*if (hasPreview) {
            return new ThemeOverride.SettingsTransparent();
        } else {*/
        return super.getThemeSet();
        //}
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_action_bar) {
            startActivity(new Intent(this, SettingsSearchActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldShowSearch()) {
            Toolbar toolbar = findViewById(R.id.search_action_bar);
            toolbar.getMenu().clear();
            OmegaPreferences prefs = Utilities.getOmegaPrefs(this);

            toolbar.inflateMenu(R.menu.menu_settings);
            ActionMenuView menuView = null;
            int count = toolbar.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof ActionMenuView) {
                    menuView = (ActionMenuView) child;
                    break;
                }
            }
            if (!BuildConfig.APPLICATION_ID.equals(resolveDefaultHome())) {
                toolbar.inflateMenu(R.menu.menu_change_default_home);
            }
            toolbar.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.action_change_default_home:
                        FakeLauncherKt.changeDefaultHome(this);
                        break;
                    case R.id.action_restart_launcher:
                        Utilities.killLauncher();
                        break;
                    case R.id.action_dev_options:
                        Intent intent = new Intent(this, SettingsActivity.class);
                        intent.putExtra(SettingsActivity.SubSettingsFragment.TITLE,
                                getString(R.string.developer_options_title));
                        intent.putExtra(SettingsActivity.SubSettingsFragment.CONTENT_RES_ID,
                                R.xml.omega_preferences_developer);
                        intent.putExtra(SettingsBaseActivity.EXTRA_FROM_SETTINGS, true);
                        startActivity(intent);
                        break;
                    default:
                        return false;
                }
                return true;
            });
        }
    }

    private String resolveDefaultHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info = getPackageManager()
                .resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (info != null && info.activityInfo != null) {
            return info.activityInfo.packageName;
        } else {
            return null;
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {
        Fragment fragment;
        if (preference instanceof SubPreference) {
            ((SubPreference) preference).start(this);
            return true;
        } else {
            fragment = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
        }
        if (fragment instanceof DialogFragment) {
            ((DialogFragment) fragment).show(getSupportFragmentManager(), preference.getKey());
        } else {
            startFragment(this, preference.getFragment(), preference.getExtras(), preference.getTitle());
        }
        return true;
    }

    @Override
    public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragment caller, Preference pref) {
        return false;
    }

    public static void startFragment(Context context, String fragment, int title) {
        startFragment(context, fragment, null, context.getString(title));
    }

    public static void startFragment(Context context, String fragment, @Nullable Bundle args) {
        startFragment(context, fragment, args, null);
    }

    public static void startFragment(Context context, String fragment, @Nullable Bundle args, @Nullable CharSequence title) {
        context.startActivity(createFragmentIntent(context, fragment, args, title));
    }

    @NotNull
    private static Intent createFragmentIntent(Context context, String fragment, @Nullable Bundle args, @Nullable CharSequence title) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_FRAGMENT, fragment);
        intent.putExtra(EXTRA_FRAGMENT_ARGS, args);
        if (title != null) {
            intent.putExtra(EXTRA_TITLE, title);
        }
        return intent;
    }

    protected Fragment createLaunchFragment(Intent intent) {
        CharSequence title = intent.getCharSequenceExtra(EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }
        String fragment = intent.getStringExtra(EXTRA_FRAGMENT);
        if (fragment != null) {
            return Fragment.instantiate(this, fragment, intent.getBundleExtra(EXTRA_FRAGMENT_ARGS));
        }
        int content = intent.getIntExtra(SubSettingsFragment.CONTENT_RES_ID, 0);
        return content != 0
                ? SubSettingsFragment.newInstance(getIntent())
                : new LauncherSettingsFragment();
    }

    public abstract static class BaseFragment extends PreferenceFragmentCompat {
        private static final String SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted";
        private HighlightablePreferenceGroupAdapter mAdapter;
        private boolean mPreferenceHighlighted = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (savedInstanceState != null) {
                mPreferenceHighlighted = savedInstanceState.getBoolean(SAVE_HIGHLIGHTED_KEY);
            }
        }

        @SuppressLint("RestrictedApi")
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent,
                                                 Bundle savedInstanceState) {
            RecyclerView recyclerView = (RecyclerView) inflater
                    .inflate(getRecyclerViewLayoutRes(), parent, false);
            if (recyclerView instanceof SpringRecyclerView) {
                ((SpringRecyclerView) recyclerView).setShouldTranslateSelf(false);
            }

            recyclerView.setLayoutManager(onCreateLayoutManager());
            recyclerView.setAccessibilityDelegateCompat(
                    new PreferenceRecyclerViewAccessibilityDelegate(recyclerView));

            return recyclerView;
        }

        abstract protected int getRecyclerViewLayoutRes();

        @Override
        public void setDivider(Drawable divider) {
            super.setDivider(null);
        }

        @Override
        public void setDividerHeight(int height) {
            super.setDividerHeight(0);
        }

        public void highlightPreferenceIfNeeded() {
            if (!isAdded()) {
                return;
            }
            if (mAdapter != null) {
                mAdapter.requestHighlight(Objects.requireNonNull(getView()), getListView());
            }
        }

        protected void onDataSetChanged() {
            highlightPreferenceIfNeeded();
        }

        public int getInitialExpandedChildCount() {
            return -1;
        }

        @Override
        public void onResume() {
            super.onResume();
            highlightPreferenceIfNeeded();

            dispatchOnResume(getPreferenceScreen());
        }

        public void dispatchOnResume(PreferenceGroup group) {
            int count = group.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference preference = group.getPreference(i);

                if (preference instanceof PreferenceGroup) {
                    dispatchOnResume((PreferenceGroup) preference);
                }
            }
        }

        void onPreferencesAdded(PreferenceGroup group) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                Preference preference = group.getPreference(i);

                if (preference instanceof ControlledPreference) {
                    PreferenceController controller = ((ControlledPreference) preference)
                            .getController();
                    if (controller != null) {
                        if (!controller.onPreferenceAdded(preference)) {
                            i--;
                            continue;
                        }
                    }
                }

                if (preference instanceof PreferenceGroup) {
                    onPreferencesAdded((PreferenceGroup) preference);
                }
            }
        }
    }

    public static class LauncherSettingsFragment extends BaseFragment {
        private boolean mShowDevOptions;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mShowDevOptions = Utilities.getOmegaPrefs(getActivity()).getDeveloperOptionsEnabled();
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.omega_preferences);
            onPreferencesAdded(getPreferenceScreen());
        }

        @Override
        public void onResume() {
            super.onResume();
            boolean dev = Utilities.getOmegaPrefs(getActivity()).getDeveloperOptionsEnabled();
            if (dev != mShowDevOptions) {
                getActivity().recreate();
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }

        @Override
        protected int getRecyclerViewLayoutRes() {
            return BuildConfig.FEATURE_SETTINGS_SEARCH ? R.layout.preference_home_recyclerview
                    : R.layout.preference_dialog_recyclerview;
        }
    }

    public static class SubSettingsFragment extends BaseFragment implements
            Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        public static final String TITLE = "title";
        public static final String CONTENT_RES_ID = "content_res_id";
        public static final String HAS_PREVIEW = "has_preview";

        private SystemDisplayRotationLockObserver mRotationLockObserver;

        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mContext = getActivity();
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            int preference = getContent();
            ContentResolver resolver = mContext.getContentResolver();
            switch (preference) {
                case R.xml.omega_preferences_desktop:
                    if (!Utilities.ATLEAST_OREO) {
                        getPreferenceScreen().removePreference(
                                findPreference(SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY));
                    }
                    // Setup allow rotation preference
                    Preference rotationPref = findPreference(Utilities.ALLOW_ROTATION_PREFERENCE_KEY);
                    if (getResources().getBoolean(R.bool.allow_rotation)) {
                        // Launcher supports rotation by default. No need to show this setting.
                        getPreferenceScreen().removePreference(rotationPref);
                    } else {
                        mRotationLockObserver = new SystemDisplayRotationLockObserver(rotationPref, resolver);

                        // Register a content observer to listen for system setting changes while
                        // this UI is active.
                        mRotationLockObserver.register(Settings.System.ACCELEROMETER_ROTATION);

                        // Initialize the UI once
                        rotationPref.setDefaultValue(Utilities.getAllowRotationDefaultValue(getActivity()));
                    }
                    break;
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference instanceof ColorPreferenceCompat) {
                ColorPickerDialog dialog = ((ColorPreferenceCompat) preference).getDialog();
                dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    public void onColorSelected(int dialogId, int color) {
                        ((ColorPreferenceCompat) preference).saveValue(color);
                    }

                    public void onDialogDismissed(int dialogId) {
                    }
                });
                dialog.show((getActivity()).getSupportFragmentManager(), "color-picker-dialog");
            } else if (preference.getFragment() != null) {
                Log.d("Settings", "Opening Fragment: " + preference.getFragment());
                SettingsActivity.startFragment(getContext(), preference.getFragment(), null, preference.getTitle());
            }
            return false;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(getContent());
            onPreferencesAdded(getPreferenceScreen());
        }

        private int getContent() {
            return getArguments().getInt(CONTENT_RES_ID);
        }

        @Override
        public void onResume() {
            super.onResume();
            setActivityTitle();
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            final DialogFragment f;
            if (preference instanceof ListPreference) {
                Log.d("success", "onDisplayPreferenceDialog: yay");
                f = ThemedListPreferenceDialogFragment.Companion.newInstance(preference.getKey());
            } else {
                super.onDisplayPreferenceDialog(preference);
                return;
            }

            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            return false;
        }

        protected void setActivityTitle() {
            getActivity().setTitle(getArguments().getString(TITLE));
        }

        public static SubSettingsFragment newInstance(SubPreference preference) {
            SubSettingsFragment fragment = new SubSettingsFragment();
            Bundle b = new Bundle(2);
            b.putString(TITLE, (String) preference.getTitle());
            b.putInt(CONTENT_RES_ID, preference.getContent());
            fragment.setArguments(b);
            return fragment;
        }

        public static SubSettingsFragment newInstance(Intent intent) {
            SubSettingsFragment fragment = new SubSettingsFragment();
            Bundle b = new Bundle(2);
            b.putString(TITLE, intent.getStringExtra(TITLE));
            b.putInt(CONTENT_RES_ID, intent.getIntExtra(CONTENT_RES_ID, 0));
            fragment.setArguments(b);
            return fragment;
        }

        protected int getRecyclerViewLayoutRes() {
            return R.layout.preference_insettable_recyclerview;
        }
    }

    /**
     * Content observer which listens for system auto-rotate setting changes, and enables/disables
     * the launcher rotation setting accordingly.
     */
    private static class SystemDisplayRotationLockObserver extends SettingsObserver.System {

        private final Preference mRotationPref;

        public SystemDisplayRotationLockObserver(
                Preference rotationPref, ContentResolver resolver) {
            super(resolver);
            mRotationPref = rotationPref;
        }

        @Override
        public void onSettingChanged(boolean enabled) {
            mRotationPref.setEnabled(enabled);
            mRotationPref.setSummary(enabled
                    ? R.string.allow_rotation_desc : R.string.allow_rotation_blocked_desc);
        }
    }
}
