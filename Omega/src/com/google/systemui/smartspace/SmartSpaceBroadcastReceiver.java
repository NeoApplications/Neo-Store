package com.google.systemui.smartspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard;

public class SmartSpaceBroadcastReceiver extends BroadcastReceiver {

    private void cg(SmartSpaceCard b, Context context, Intent intent, boolean b2) throws PackageManager.NameNotFoundException {
        if (b.getShouldDiscard()) {
            SmartSpaceController.get(context).cV(null);
            return;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.google.android.googlequicksearchbox", 0);
            SmartSpaceController.get(context).cV(new NewCardInfo(b, intent, b2, SystemClock.uptimeMillis(), packageInfo));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    public void onReceive(Context context, Intent intent) {
        byte[] byteArrayExtra = intent.getByteArrayExtra("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CARD");
        if (byteArrayExtra != null) {
            SmartSpaceUpdate.Builder builder = SmartSpaceUpdate.newBuilder();
            try {
                //mergeFrom(builder.build(), byteArrayExtra);
                SmartSpaceCard[] cw = builder.getCardList().toArray(new SmartSpaceCard[0]);
                int length = cw.length;
                int i = 0;
                while (i < length) {
                    SmartSpaceCard b2 = cw[i];
                    boolean b3 = b2.getCardPriority() == 1;
                    if (b3 || b2.getCardPriority() == 2) {
                        cg(b2, context, intent, b3);
                    } else {
                        Log.w("SmartspaceReceiver", "unrecognized card priority");
                    }
                    ++i;
                }
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e("SmartspaceReceiver", "proto", ex);
            }
        } else {
            Log.e("SmartspaceReceiver", "receiving update with no proto: " + intent.getExtras());
        }
    }
}
