/*
 * Copyright (c) 2020 Omega Launcher
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
 */

package com.saggitt.omega.smartspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.apps.nexuslauncher.smartspace.nano.SmartspaceProto.b;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.saggitt.omega.util.Config;

public class SmartspaceBroadcastReceiver extends BroadcastReceiver {
    private void cg(b b, Context context, Intent intent, boolean b2) throws PackageManager.NameNotFoundException {
        if (b.cy) {
            SmartspaceController.get(context).updateData(null);
            return;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(Config.GOOGLE_QSB, 0);
            SmartspaceController.get(context).updateData(new NewCardInfo(b, intent, b2, SystemClock.uptimeMillis(), packageInfo));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    public void onReceive(Context context, Intent intent) {
        byte[] byteArrayExtra = intent.getByteArrayExtra("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CARD");
        if (byteArrayExtra != null) {
            com.google.android.apps.nexuslauncher.smartspace.nano.SmartspaceProto.a a = new com.google.android.apps.nexuslauncher.smartspace.nano.SmartspaceProto.a();
            try {
                com.google.protobuf.nano.MessageNano.mergeFrom(a, byteArrayExtra);
                b[] cw = a.cw;
                int length = cw.length;
                int i = 0;
                while (i < length) {
                    b b2 = cw[i];
                    boolean b3 = b2.cz == 1;
                    if (b3 || b2.cz == 2) {
                        cg(b2, context, intent, b3);
                    } else {
                        Log.w("SmartspaceReceiver", "unrecognized card priority");
                    }
                    ++i;
                }
            } catch (InvalidProtocolBufferNanoException | PackageManager.NameNotFoundException ex) {
                Log.e("SmartspaceReceiver", "proto", ex);
            }
        } else {
            Log.e("SmartspaceReceiver", "receiving update with no proto: " + intent.getExtras());
        }
    }
}
