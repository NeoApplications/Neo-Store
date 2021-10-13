package app.lawnchair.compatlib;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.RemoteException;

import java.util.List;

public abstract class ActivityManagerCompat {

    public abstract void startRecentsActivity(Intent intent, long eventTime, RecentsAnimationRunnerStub runner) throws RemoteException;

    public abstract ActivityManager.RunningTaskInfo getRunningTask(boolean filterOnlyVisibleRecents);

    public abstract List<ActivityManager.RecentTaskInfo> getRecentTasks(int numTasks, int userId);
}
