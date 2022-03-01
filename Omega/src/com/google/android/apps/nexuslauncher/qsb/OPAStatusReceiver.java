package com.google.android.apps.nexuslauncher.qsb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.launcher3.Utilities;

public class OPAStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Utilities.getDevicePrefs(context).edit().putBoolean("opa_enabled", intent.getBooleanExtra("OPA_ENABLED", false)).apply();
    }
}
