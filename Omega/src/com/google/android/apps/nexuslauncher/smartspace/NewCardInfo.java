package com.google.android.apps.nexuslauncher.smartspace;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.b;

public class NewCardInfo {
    public final b di;
    public final boolean dj;
    public final PackageInfo dk;
    public final long dl;
    public final Intent intent;

    public NewCardInfo(b di, Intent intent, boolean dj, long dl, PackageInfo dk) {
        this.di = di;
        this.dj = dj;
        this.intent = intent;
        this.dl = dl;
        this.dk = dk;
    }

    private static Object getParcelableExtra(String name, Intent intent) {
        if (!TextUtils.isEmpty(name)) {
            return intent.getParcelableExtra(name);
        }
        return null;
    }

    public Bitmap getBitmap(final Context context) {
        com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.f fVar = this.di.getCx();
        if (fVar == null) {
            return null;
        }
        Bitmap bitmap = (Bitmap) getParcelableExtra(fVar.getCV(), this.intent);
        if (bitmap != null) {
            return bitmap;
        }
        try {
            if (TextUtils.isEmpty(fVar.getCW())) {
                /*if (!TextUtils.isEmpty(fVar.getCX())) {
                    Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication("com.google.android.googlequicksearchbox");
                    return LauncherIcons.obtain(context).createIconBitmap(
                            resourcesForApplication.getDrawableForDensity(resourcesForApplication.getIdentifier(fVar.getCX(), null, null),
                                    LauncherAppState.getIDP(context).fillResIconDpi), 1f);
                }*/
                return null;
            }
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(fVar.getCW()));
        } catch (Exception e) {
            Log.e("NewCardInfo", "retrieving bitmap uri=" + fVar.getCW() + " gsaRes=" + fVar.getCX());
            return null;
        }
    }
}
