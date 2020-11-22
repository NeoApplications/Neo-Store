package com.google.android.apps.nexuslauncher.smartspace;

public interface ISmartspace {
    void onGsaChanged();

    void postUpdate(final SmartspaceDataContainer p0);
}
