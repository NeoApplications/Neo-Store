package com.google.systemui.smartspace;

import static com.android.launcher3.uioverrides.DejankBinderTracker.isMainThread;
import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;

import com.android.launcher3.Alarm;
import com.google.android.apps.nexuslauncher.utils.ActionIntentFilter;
import com.google.android.systemui.smartspace.SmartspaceProto.CardWrapper;
import com.saggitt.omega.util.Config;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SmartSpaceController implements Handler.Callback {
    enum Store {
        WEATHER("smartspace_weather"),
        CURRENT("smartspace_current");

        final String filename;

        Store(final String filename) {
            this.filename = filename;
        }
    }

    private static SmartSpaceController sInstance;
    private final SmartSpaceData mData;
    private final ProtoStore mProtoStore;
    private final Context mContext;
    private final Handler mUiHandler;
    private final Handler mWorker;

    private final ArrayList<SmartSpaceUpdateListener> mListeners = new ArrayList<>();
    public int mCurrentUserId;
    private boolean mHidePrivateData;
    private final Handler mBackgroundHandler;

    private final Alarm mAlarm;

    public SmartSpaceController(Context context) {
        mContext = context;
        mProtoStore = new ProtoStore(mContext);
        mWorker = new Handler(MODEL_EXECUTOR.getLooper());
        mUiHandler = new Handler(Looper.getMainLooper());

        mCurrentUserId = UserHandle.myUserId();
        HandlerThread handlerThread = new HandlerThread("smartspace-background");
        handlerThread.start();
        mBackgroundHandler = new Handler(handlerThread.getLooper());

        mData = new SmartSpaceData();

        (mAlarm = new Alarm()).setOnAlarmListener(alarm -> onExpire());
        updateGsa();
        mContext.registerReceiver(new BroadcastReceiver() {
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

    public static SmartSpaceController get(final Context context) {
        if (sInstance == null) {
            sInstance = new SmartSpaceController(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onNewCard(final NewCardInfo newCardInfo) {
        if (newCardInfo != null) {
            if (newCardInfo.getUserId() != mCurrentUserId) {
                return;
            }
            mBackgroundHandler.post(() -> {
                final CardWrapper wrapper = newCardInfo.toWrapper(mContext);
                if (!mHidePrivateData) {
                    ProtoStore protoStore = mProtoStore;
                    StringBuilder sb = new StringBuilder();
                    sb.append("smartspace_");
                    sb.append(mCurrentUserId);
                    sb.append("_");
                    sb.append(newCardInfo.isPrimary());
                    //TODO Revisar el Uso de MessageNano
                    //protoStore.store(wrapper, sb.toString());
                }
                mUiHandler.post(() -> {
                    SmartSpaceCardView smartSpaceCard = newCardInfo.shouldDiscard() ? null :
                            SmartSpaceCardView.fromWrapper(mContext, wrapper, newCardInfo.isPrimary());
                    if (newCardInfo.isPrimary()) {
                        mData.mCurrentCard = smartSpaceCard;
                    } else {
                        mData.mWeatherCard = smartSpaceCard;
                    }
                    mData.handleExpire();
                    update();
                });
            });
        }
    }

    private SmartSpaceCardView loadSmartSpaceData(boolean z) {
        CardWrapper cardWrapper = CardWrapper.newBuilder().build();
        ProtoStore protoStore = mProtoStore;
        StringBuilder sb = new StringBuilder();
        sb.append("smartspace_");
        sb.append(mCurrentUserId);
        sb.append("_");
        sb.append(z);
        if (protoStore.load(sb.toString(), cardWrapper)) {
            return SmartSpaceCardView.fromWrapper(mContext, cardWrapper, !z);
        }
        return null;
    }

    public void reloadData() {
        mData.mCurrentCard = loadSmartSpaceData(true);
        mData.mWeatherCard = loadSmartSpaceData(false);
        update();
    }

    public void addListener(SmartSpaceUpdateListener smartSpaceUpdateListener) {
        isMainThread();
        mListeners.add(smartSpaceUpdateListener);
        SmartSpaceData smartSpaceData = mData;

        if (smartSpaceData != null && smartSpaceUpdateListener != null) {
            smartSpaceUpdateListener.onSmartSpaceUpdated(smartSpaceData);
        }
    }

    public void removeListener(SmartSpaceUpdateListener smartSpaceUpdateListener) {
        isMainThread();
        mListeners.remove(smartSpaceUpdateListener);
    }

    private void onExpire() {
        boolean hasWeather = mData.hasWeather();
        boolean hasCurrent = mData.hasCurrent();
        mData.handleExpire();

        if (hasWeather && !mData.hasWeather()) {
            df(null, SmartSpaceController.Store.WEATHER);
        }

        if (hasCurrent && !mData.hasCurrent()) {
            df(null, SmartSpaceController.Store.CURRENT);
            mContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.EXPIRE_EVENT")
                    .setPackage(Config.GOOGLE_QSB)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public void setHideSensitiveData(boolean z) {
        mHidePrivateData = z;
        ArrayList<SmartSpaceUpdateListener> arrayList = new ArrayList<>(mListeners);
        for (int i = 0; i < arrayList.size(); i++) {
            arrayList.get(i).onSensitiveModeChanged(z);
        }
        if (mHidePrivateData) {
            clearStore();
        }
    }

    private void clearStore() {
        ProtoStore protoStore = mProtoStore;
        String str = "smartspace_";
        String sb = "smartspace_" + mCurrentUserId + "_true";
        protoStore.store(null, sb);
        ProtoStore protoStore2 = mProtoStore;
        String sb2 = str + mCurrentUserId + "_false";
        protoStore2.store(null, sb2);
    }

    private void update() {
        mAlarm.cancelAlarm();

        long expiresAtMillis = mData.getExpiresAtMillis();

        if (expiresAtMillis > 0) {
            mAlarm.setAlarm(expiresAtMillis);
        }

        ArrayList<SmartSpaceUpdateListener> listeners = new ArrayList<>(mListeners);
        for (SmartSpaceUpdateListener listener : listeners) {
            listener.onSmartSpaceUpdated(mData);
        }
    }

    public boolean handleMessage(Message message) {
        SmartSpaceCardView cardView = null;

        switch (message.what) {
            case 1:
                CardWrapper data = CardWrapper.newBuilder().build();
                SmartSpaceCardView weatherCard = mProtoStore.load(SmartSpaceController.Store.WEATHER.filename, data) ?
                        SmartSpaceCardView.fromWrapper(mContext, data, true) :
                        null;

                data = CardWrapper.newBuilder().build();
                SmartSpaceCardView eventCard = mProtoStore.load(SmartSpaceController.Store.CURRENT.filename, data) ?
                        SmartSpaceCardView.fromWrapper(mContext, data, false) :
                        null;

                Message.obtain(mUiHandler, 101, new SmartSpaceCardView[]{weatherCard, eventCard}).sendToTarget();
                break;
            case 2:
                //mProtoStore.store(SmartSpaceCardView.cQ(mContext, (NewCardInfo) message.obj), SmartSpaceController.Store.values()[message.arg1].filename);
                Message.obtain(mUiHandler, 1).sendToTarget();
                break;
            case 101:
                SmartSpaceCardView[] cardViews = (SmartSpaceCardView[]) message.obj;
                if (cardViews != null) {
                    mData.mWeatherCard = cardViews.length > 0 ?
                            cardViews[0] :
                            null;

                    if (cardViews.length > 1) {
                        cardView = cardViews[1];
                    }

                    mData.mCurrentCard = cardView;
                }
                mData.handleExpire();
                update();
                break;
        }

        return true;
    }

    private Intent db() {
        return new Intent("com.google.android.apps.gsa.smartspace.SETTINGS")
                .setPackage(Config.GOOGLE_QSB)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    private void updateGsa() {
        ArrayList<SmartSpaceUpdateListener> listeners = new ArrayList<>(mListeners);
        for (SmartSpaceUpdateListener listener : listeners) {
            listener.onGsaChanged();
        }
        onGsaChanged();
    }


    private void onGsaChanged() {
        Log.d("SmartSpaceController", "onGsaChanged");
        mContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.ENABLE_UPDATE")
                .setPackage(Config.GOOGLE_QSB)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void df(NewCardInfo newCardInfo, SmartSpaceController.Store controller) {
        Message.obtain(mWorker, 2, controller.ordinal(), 0, newCardInfo).sendToTarget();
    }


    public void cW() {
        Message.obtain(this.mWorker, 1).sendToTarget();
    }

    public void cX(final String s, final PrintWriter printWriter) {
        printWriter.println();
        printWriter.println(s + "SmartspaceController");
        printWriter.println(s + "  weather " + this.mData.mCurrentCard);
        printWriter.println(s + "  current " + this.mData.mWeatherCard);
    }

    public boolean cY() {
        boolean b = false;
        final List queryBroadcastReceivers = this.mContext.getPackageManager().queryBroadcastReceivers(db(), 0);
        if (queryBroadcastReceivers != null) {
            b = !queryBroadcastReceivers.isEmpty();
        }
        return b;
    }
}
