package com.google.systemui.smartspace;

import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.launcher3.Alarm;
import com.google.android.apps.nexuslauncher.utils.ActionIntentFilter;
import com.google.android.apps.nexuslauncher.utils.ProtoStore;
import com.saggitt.omega.smartspace.FeedBridge;

import java.io.PrintWriter;
import java.util.List;

public class SmartSpaceController {
    enum Store {
        WEATHER("smartspace_weather"),
        CURRENT("smartspace_current");

        final String filename;

        Store(final String filename) {
            this.filename = filename;
        }
    }

    private static SmartSpaceController dU;
    private final SmartSpaceDataContainer dQ;
    private final Alarm dR;
    private final ProtoStore dT;
    private SmartSpaceUpdateListener dS;
    private final Context mAppContext;
    private final Handler mUiHandler;
    private final Handler mWorker;

    public SmartSpaceController(final Context mAppContext) {
        this.mWorker = new Handler(MODEL_EXECUTOR.getLooper());
        this.mUiHandler = new Handler(Looper.getMainLooper());
        this.mAppContext = mAppContext;
        this.dQ = new SmartSpaceDataContainer();
        this.dT = new ProtoStore(mAppContext);
        (this.dR = new Alarm()).setOnAlarmListener(alarm -> dc());
        this.dd();
        mAppContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dd();
            }
        }, ActionIntentFilter.googleInstance(
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_CHANGED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_DATA_CLEARED));
    }

    public static SmartSpaceController get(final Context context) {
        if (SmartSpaceController.dU == null) {
            SmartSpaceController.dU = new SmartSpaceController(context.getApplicationContext());
        }
        return SmartSpaceController.dU;
    }

    private Intent db() {
        return new Intent("com.google.android.apps.gsa.smartspace.SETTINGS")
                .setPackage(FeedBridge.Companion.getInstance(mAppContext).resolveSmartspace())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private void dc() {
        final boolean cr = this.dQ.isWeatherAvailable();
        final boolean cs = this.dQ.cS();
        this.dQ.cU();
        if (cr && !this.dQ.isWeatherAvailable()) {
            this.df(null, SmartSpaceController.Store.WEATHER);
        }
        if (cs && !this.dQ.cS()) {
            this.df(null, SmartSpaceController.Store.CURRENT);
            this.mAppContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.EXPIRE_EVENT")
                    .setPackage(FeedBridge.Companion.getInstance(mAppContext).resolveSmartspace())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void dd() {
        if (this.dS != null) {
            this.dS.onGsaChanged();
        }
        this.de();
    }

    private void de() {
        this.mAppContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.ENABLE_UPDATE")
                .setPackage(FeedBridge.Companion.getInstance(mAppContext).resolveSmartspace())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void df(final NewCardInfo a, final SmartSpaceController.Store SmartspaceControllerStore) {
        Message.obtain(this.mWorker, 2, SmartspaceControllerStore.ordinal(), 0, a).sendToTarget();
    }

    private void update() {
        this.dR.cancelAlarm();
        final long ct = this.dQ.cT();
        if (ct > 0L) {
            this.dR.setAlarm(ct);
        }
        if (this.dS != null) {
            this.dS.onSmartSpaceUpdated(this.dQ);
        }
    }

    public void cV(final NewCardInfo a) {
        if (a != null && !a.mIsPrimary) {
            this.df(a, SmartSpaceController.Store.WEATHER);
        } else {
            this.df(a, SmartSpaceController.Store.CURRENT);
        }
    }

    public void cW() {
        Message.obtain(this.mWorker, 1).sendToTarget();
    }

    public void cX(final String s, final PrintWriter printWriter) {
        printWriter.println();
        printWriter.println(s + "SmartspaceController");
        printWriter.println(s + "  weather " + this.dQ.dO);
        printWriter.println(s + "  current " + this.dQ.dP);
    }

    public boolean cY() {
        boolean b = false;
        final List queryBroadcastReceivers = this.mAppContext.getPackageManager().queryBroadcastReceivers(this.db(), 0);
        if (queryBroadcastReceivers != null) {
            b = !queryBroadcastReceivers.isEmpty();
        }
        return b;
    }

    public void da(final SmartSpaceUpdateListener ds) {
        this.dS = ds;
        if (this.dS != null && this.dQ != null) {
            this.dS.onSmartSpaceUpdated(this.dQ);
            this.dS.onGsaChanged();
        }
    }

    /*
    public boolean handleMessage(final Message message) {
        SmartspaceCard dVar = null;
        switch (message.what) {
            case 1:
                CardWrapper data = CardWrapper.newBuilder().build();
                SmartspaceCard weatherCard = this.dT.load(SmartspaceController.Store.WEATHER.filename, data) ?
                        SmartspaceCard.cD(this.mAppContext, data, true) :
                        null;

                data = CardWrapper.newBuilder().build();
                SmartspaceCard eventCard = this.dT.load(SmartspaceController.Store.CURRENT.filename, data) ?
                        SmartspaceCard.cD(this.mAppContext, data, false) :
                        null;

                Message.obtain(this.mUiHandler, 101, new SmartspaceCard[]{weatherCard, eventCard}).sendToTarget();
                break;
            case 2:
                dT.store(SmartspaceCard.cQ(this.mAppContext, (NewCardInfo) message.obj), SmartspaceController.Store.values()[message.arg1].filename);
                Message.obtain(this.mUiHandler, 1).sendToTarget();
                break;
            case 101:
                SmartspaceCard[] dVarArr = (SmartspaceCard[]) message.obj;
                if (dVarArr != null) {
                    this.dQ.dO = dVarArr.length > 0 ?
                            dVarArr[0] :
                            null;

                    SmartspaceDataContainer eVar = this.dQ;
                    if (dVarArr.length > 1) {
                        dVar = dVarArr[1];
                    }

                    eVar.dP = dVar;
                }
                this.dQ.cU();
                update();
                break;
        }
        return true;
    }
    */
}
