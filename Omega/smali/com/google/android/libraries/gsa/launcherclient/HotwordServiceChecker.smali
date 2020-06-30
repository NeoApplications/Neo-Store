.class public Lcom/google/android/libraries/gsa/launcherclient/HotwordServiceChecker;
.super Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;
.source "HotwordServiceChecker.java"


# annotations
.annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
.end annotation


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .registers 2
    .param p1, "context"    # Landroid/content/Context;
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 32
    invoke-direct {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;-><init>(Landroid/content/Context;)V

    .line 33
    return-void
.end method


# virtual methods
.method public checkHotwordService(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V
    .registers 3
    .param p1, "statusCallback"    # Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 41
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/HotwordServiceChecker;->context:Landroid/content/Context;

    invoke-static {v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->getServiceIntent(Landroid/content/Context;)Landroid/content/Intent;

    move-result-object v0

    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/gsa/launcherclient/HotwordServiceChecker;->checkServiceStatus(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;Landroid/content/Intent;)V

    .line 42
    return-void
.end method

.method protected getStatus(Landroid/os/IBinder;)Z
    .registers 3
    .param p1, "service"    # Landroid/os/IBinder;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .prologue
    .line 46
    invoke-static {p1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->asInterface(Landroid/os/IBinder;)Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    move-result-object v0

    invoke-interface {v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->isVoiceDetectionRunning()Z

    move-result v0

    return v0
.end method
