package com.saggitt.omega.smartspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.launcher3.Alarm;
import com.saggitt.omega.util.ActionIntentFilter;

import java.io.PrintWriter;
import java.util.List;

import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;
import static com.google.android.apps.nexuslauncher.smartspace.nano.SmartspaceProto.i;

public class SmartspaceController implements Handler.Callback {
    enum Store {
        WEATHER("smartspace_weather"),
        CURRENT("smartspace_current");

        final String filename;

        Store(final String filename) {
            this.filename = filename;
        }
    }

    private static SmartspaceController INSTANCE;
    private final SmartspaceDataContainer dataContainer;
    private final Alarm refreshAlarm;
    private ISmartspace mSmartspace;
    private final ProtoStore protoStore;
    private final Context mAppContext;
    private final Handler mUiHandler;
    private final Handler mWorker;

    public SmartspaceController(final Context mAppContext) {
        this.mWorker = new Handler(MODEL_EXECUTOR.getLooper(), this);
        this.mUiHandler = new Handler(Looper.getMainLooper(), this);
        this.mAppContext = mAppContext;
        this.dataContainer = new SmartspaceDataContainer();
        this.protoStore = new ProtoStore(mAppContext);
        (this.refreshAlarm = new Alarm()).setOnAlarmListener(alarm -> refresh());
        this.updateGsa();
        mAppContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateGsa();
            }
        }, ActionIntentFilter.googleInstance(
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_CHANGED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_DATA_CLEARED));
    }

    private Intent getSmartspaceOptionsIntent() {
        return new Intent("com.google.android.apps.gsa.smartspace.SETTINGS")
                .setPackage(FeedBridge.Companion.getInstance(mAppContext).resolveSmartspace())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private void refresh() {
        final boolean weatherAvailable = this.dataContainer.isWeatherAvailable();
        final boolean dataAvailable = this.dataContainer.isDataAvailable();
        dataContainer.clearAll();
        if (weatherAvailable && !this.dataContainer.isWeatherAvailable()) {
            this.updateSmartspaceStore(null, SmartspaceController.Store.WEATHER);
        }
        if (dataAvailable && !this.dataContainer.isDataAvailable()) {
            updateSmartspaceStore(null, SmartspaceController.Store.CURRENT);
            mAppContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.EXPIRE_EVENT")
                    .setPackage(FeedBridge.Companion.getInstance(mAppContext).resolveSmartspace())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void updateGsa() {
        if (this.mSmartspace != null) {
            this.mSmartspace.onGsaChanged();
        }
        this.onPostGsaUpdate();
    }

    private void onPostGsaUpdate() {
        mAppContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.ENABLE_UPDATE")
                .setPackage(FeedBridge.Companion.getInstance(mAppContext).resolveSmartspace())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void updateSmartspaceStore(final NewCardInfo a, final SmartspaceController.Store SmartspaceControllerStore) {
        Message.obtain(this.mWorker, 2, SmartspaceControllerStore.ordinal(), 0, a).sendToTarget();
    }

    public static SmartspaceController get(final Context context) {
        if (SmartspaceController.INSTANCE == null) {
            SmartspaceController.INSTANCE = new SmartspaceController(context.getApplicationContext());
        }
        return SmartspaceController.INSTANCE;
    }

    private void update() {
        this.refreshAlarm.cancelAlarm();
        final long ct = this.dataContainer.cT();
        if (ct > 0L) {
            this.refreshAlarm.setAlarm(ct);
        }
        if (this.mSmartspace != null) {
            this.mSmartspace.postUpdate(this.dataContainer);
        }
    }

    public void updateData(final NewCardInfo a) {
        if (a != null && !a.forWeather) {
            this.updateSmartspaceStore(a, SmartspaceController.Store.WEATHER);
        } else {
            this.updateSmartspaceStore(a, SmartspaceController.Store.CURRENT);
        }
    }

    public void sendMessage() {
        Message.obtain(this.mWorker, 1).sendToTarget();
    }

    public void dumpInfo(final String s, final PrintWriter printWriter) {
        printWriter.println();
        printWriter.println(s + "SmartspaceController");
        printWriter.println(s + "  weather " + this.dataContainer.dO);
        printWriter.println(s + "  current " + this.dataContainer.dP);
    }

    public boolean cY() {
        boolean b = false;
        final List queryBroadcastReceivers = this.mAppContext.getPackageManager()
                .queryBroadcastReceivers(getSmartspaceOptionsIntent(), 0);
        if (queryBroadcastReceivers != null) {
            b = !queryBroadcastReceivers.isEmpty();
        }
        return b;
    }

    public void da(final ISmartspace ds) {
        this.mSmartspace = ds;
        if (this.mSmartspace != null && this.dataContainer != null) {
            this.mSmartspace.postUpdate(this.dataContainer);
            this.mSmartspace.onGsaChanged();
        }
    }

    public boolean handleMessage(final Message message) {
        SmartspaceCard dVar = null;
        switch (message.what) {
            case 1:
                i data = new i();
                SmartspaceCard weatherCard = this.protoStore.dv(SmartspaceController.Store.WEATHER.filename, data) ?
                        SmartspaceCard.cD(this.mAppContext, data, true) :
                        null;

                data = new i();
                SmartspaceCard eventCard = this.protoStore.dv(SmartspaceController.Store.CURRENT.filename, data) ?
                        SmartspaceCard.cD(this.mAppContext, data, false) :
                        null;

                Message.obtain(this.mUiHandler, 101, new SmartspaceCard[]{weatherCard, eventCard}).sendToTarget();
                break;
            case 2:
                this.protoStore.dw(SmartspaceCard.cQ(this.mAppContext, (NewCardInfo) message.obj), SmartspaceController.Store.values()[message.arg1].filename);
                Message.obtain(this.mUiHandler, 1).sendToTarget();
                break;
            case 101:
                SmartspaceCard[] dVarArr = (SmartspaceCard[]) message.obj;
                if (dVarArr != null) {
                    this.dataContainer.dO = dVarArr.length > 0 ?
                            dVarArr[0] :
                            null;

                    SmartspaceDataContainer eVar = this.dataContainer;
                    if (dVarArr.length > 1) {
                        dVar = dVarArr[1];
                    }

                    eVar.dP = dVar;
                }
                this.dataContainer.clearAll();
                update();
                break;
        }
        return true;
    }
}
