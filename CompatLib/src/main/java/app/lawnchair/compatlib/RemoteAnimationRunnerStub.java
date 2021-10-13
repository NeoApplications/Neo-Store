package app.lawnchair.compatlib;

import android.view.IRemoteAnimationFinishedCallback;
import android.view.RemoteAnimationTarget;

public interface RemoteAnimationRunnerStub {

    void onAnimationStart(int transit, RemoteAnimationTarget[] apps,
                          RemoteAnimationTarget[] wallpapers, RemoteAnimationTarget[] nonApps,
                          final IRemoteAnimationFinishedCallback finishedCallback);

    void onAnimationCancelled();
}
