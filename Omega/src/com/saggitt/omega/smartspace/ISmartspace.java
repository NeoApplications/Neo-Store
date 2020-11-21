package com.saggitt.omega.smartspace;

public interface ISmartspace {
    void onGsaChanged();

    void postUpdate(final SmartspaceDataContainer p0);
}
