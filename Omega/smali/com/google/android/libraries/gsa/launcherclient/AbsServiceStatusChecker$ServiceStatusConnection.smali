.class Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;
.super Ljava/lang/Object;
.source "AbsServiceStatusChecker.java"

# interfaces
.implements Landroid/content/ServiceConnection;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "ServiceStatusConnection"
.end annotation


# instance fields
.field private statusCallback:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

.field final synthetic this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;


# direct methods
.method public constructor <init>(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V
    .registers 3
    .param p2, "statusCallback"    # Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    .prologue
    .line 74
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 75
    iput-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->statusCallback:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    .line 76
    return-void
.end method


# virtual methods
.method public onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .registers 6
    .param p1, "cn"    # Landroid/content/ComponentName;
    .param p2, "service"    # Landroid/os/IBinder;

    .prologue
    .line 92
    :try_start_0
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->statusCallback:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    invoke-virtual {v2, p2}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->getStatus(Landroid/os/IBinder;)Z

    move-result v2

    invoke-interface {v1, v2}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;->isRunning(Z)V
    :try_end_b
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_b} :catch_13
    .catchall {:try_start_0 .. :try_end_b} :catchall_29

    .line 97
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iget-object v1, v1, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->context:Landroid/content/Context;

    invoke-virtual {v1, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    .line 100
    :goto_12
    return-void

    .line 94
    :catch_13
    move-exception v0

    .line 95
    .local v0, "e":Landroid/os/RemoteException;
    :try_start_14
    const-string v1, "AbsServiceStatusChecker"

    const-string v2, "isServiceRunning - remote call failed"

    invoke-static {v1, v2, v0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1b
    .catchall {:try_start_14 .. :try_end_1b} :catchall_29

    .line 97
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iget-object v1, v1, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->context:Landroid/content/Context;

    invoke-virtual {v1, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    .line 99
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->statusCallback:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    const/4 v2, 0x0

    invoke-interface {v1, v2}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;->isRunning(Z)V

    goto :goto_12

    .line 97
    .end local v0    # "e":Landroid/os/RemoteException;
    :catchall_29
    move-exception v1

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$ServiceStatusConnection;->this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iget-object v2, v2, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->context:Landroid/content/Context;

    invoke-virtual {v2, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    throw v1
.end method

.method public onServiceDisconnected(Landroid/content/ComponentName;)V
    .registers 2
    .param p1, "cn"    # Landroid/content/ComponentName;

    .prologue
    .line 83
    return-void
.end method
