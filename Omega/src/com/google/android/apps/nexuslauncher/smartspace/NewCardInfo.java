package com.google.android.apps.nexuslauncher.smartspace;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.systemui.smartspace.SmartspaceProto;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard;

public class NewCardInfo {
    public final SmartSpaceCard di;
    public final boolean dj;
    public final PackageInfo dk;
    public final long dl;
    public final Intent intent;

    public NewCardInfo(SmartSpaceCard di, Intent intent, boolean dj, long dl, PackageInfo dk) {
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
        SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Image fVar = this.di.getIcon();
        if (fVar == null) {
            return null;
        }
        Bitmap bitmap = (Bitmap) getParcelableExtra(fVar.getKey(), this.intent);
        if (bitmap != null) {
            return bitmap;
        }
        try {
            if (TextUtils.isEmpty(fVar.getGsaResourceName())) {
                /*if (!TextUtils.isEmpty(fVar.getCX())) {
                    Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication("com.google.android.googlequicksearchbox");
                    return LauncherIcons.obtain(context).createIconBitmap(
                            resourcesForApplication.getDrawableForDensity(resourcesForApplication.getIdentifier(fVar.getCX(), null, null),
                                    LauncherAppState.getIDP(context).fillResIconDpi), 1f);
                }*/
                return null;
            }
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(fVar.getGsaResourceName()));
        } catch (Exception e) {
            Log.e("NewCardInfo", "retrieving bitmap uri=" + fVar.getGsaResourceName() + " gsaRes=" + fVar.getUri());
            return null;
        }
    }
}
