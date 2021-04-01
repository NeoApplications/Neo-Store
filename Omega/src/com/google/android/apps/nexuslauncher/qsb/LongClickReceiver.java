package com.google.android.apps.nexuslauncher.qsb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.saggitt.omega.OmegaLauncher;

import java.lang.ref.WeakReference;

public class LongClickReceiver extends BroadcastReceiver {

    private static WeakReference<OmegaLauncher> bR = new WeakReference<>(null);

    public static void bq(final OmegaLauncher nexusLauncherActivity) {
        LongClickReceiver.bR = new WeakReference<>(nexusLauncherActivity);
    }

    public void onReceive(final Context context, final Intent intent) {
        /*final NexusLauncherActivity launcher = LongClickReceiver.bR.get();
        if (launcher != null) {
            final ComponentKey dl = AppSearchProvider.uriToComponent(intent.getData(), context);
            final LauncherActivityInfo resolveActivity = context
                    .getSystemService(LauncherApps.class)
                    .resolveActivity(new Intent(Intent.ACTION_MAIN).setComponent(dl.componentName),
                            dl.user);
            if (resolveActivity == null) {
                return;
            }
            final ItemDragListener onDragListener = new ItemDragListener(resolveActivity,
                    intent.getSourceBounds());
            onDragListener.init(launcher, false);
            launcher.getDragLayer().setOnDragListener(onDragListener);
            final ClipData clipData = new ClipData(
                    new ClipDescription("", new String[]{onDragListener.getMimeType()}),
                    new ClipData.Item(""));
            final Bundle bundle = new Bundle();
            bundle.putParcelable("clip_data", clipData);
            this.setResult(-1, null, bundle);
        }*/
    }
}
