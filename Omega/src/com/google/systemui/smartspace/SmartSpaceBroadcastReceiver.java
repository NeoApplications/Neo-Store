package com.google.systemui.smartspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.util.Log;

import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard;

public class SmartSpaceBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String str = "SmartSpaceReceiver";

        int myUserId = UserHandle.myUserId();
        String str2 = "uid";
        if (myUserId != 0) {
            String str3 = "rebroadcast";
            if (!intent.getBooleanExtra(str3, false)) {
                intent.putExtra(str3, true);
                intent.putExtra(str2, myUserId);
                context.sendBroadcast(intent);
                return;
            }
            return;
        }
        if (!intent.hasExtra(str2)) {
            intent.putExtra(str2, myUserId);
        }
        byte[] byteArrayExtra = intent.getByteArrayExtra("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CARD");
        if (byteArrayExtra != null) {
            SmartSpaceUpdate smartspaceUpdate = SmartSpaceUpdate.newBuilder().build();
            try {
                //TODO: revisar el uso de MessageNano
                //MessageNano.mergeFrom(smartspaceUpdate, byteArrayExtra);
                for (SmartSpaceUpdate.SmartSpaceCard smartspaceCard : smartspaceUpdate.getCardList()) {
                    boolean isPrimary = smartspaceCard.getCardPriority() == 1;
                    boolean z2 = smartspaceCard.getCardPriority() == 2;
                    if (!isPrimary) {
                        if (!z2) {
                            String sb = "unrecognized card priority: " + smartspaceCard.getCardPriority();
                            Log.w(str, sb);
                        }
                    }
                    notify(smartspaceCard, context, intent, isPrimary);
                }
            } catch (Exception e) {
                Log.e(str, "proto", e);
            }
        } else {
            String sb2 = "receiving update with no proto: " + intent.getExtras();
            Log.e(str, sb2);
        }
    }

    private void notify(SmartSpaceCard smartspaceCard, Context context, Intent intent, boolean isPrimary) {
        PackageInfo packageInfo;
        long currentTimeMillis = System.currentTimeMillis();
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.google.android.googlequicksearchbox", 0);
        } catch (NameNotFoundException e) {
            Log.w("SmartSpaceReceiver", "Cannot find GSA", e);
            packageInfo = null;
        }
        NewCardInfo newCardInfo = new NewCardInfo(smartspaceCard, intent, isPrimary, currentTimeMillis, packageInfo);
        SmartSpaceController.get(context).onNewCard(newCardInfo);
    }
}
