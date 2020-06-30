.class Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;
.super Ljava/lang/Object;
.source "SimpleServiceConnection.java"

# interfaces
.implements Landroid/content/ServiceConnection;


# instance fields
.field private boundSuccessfully:Z

.field private final context:Landroid/content/Context;

.field private final flags:I

.field private final bridge:Landroid/content/ServiceConnection;


# direct methods
.method constructor <init>(Landroid/content/Context;I)V
    .registers 4
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "flags"    # I

    .prologue
    .line 17
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 18
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->context:Landroid/content/Context;

    .line 19
    iput p2, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->flags:I

    new-instance v0, Lamirz/aidlbridge/LauncherClientBridge;

    invoke-direct {v0, p0, p2}, Lamirz/aidlbridge/LauncherClientBridge;-><init>(Landroid/content/ServiceConnection;I)V

    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->bridge:Landroid/content/ServiceConnection;

    .line 20
    return-void
.end method


# virtual methods
.method public final connectSafely()Z
    .registers 6

    .prologue
    .line 43
    iget-boolean v1, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->boundSuccessfully:Z

    if-nez v1, :cond_14

    .line 45
    :try_start_4
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->context:Landroid/content/Context;

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->context:Landroid/content/Context;

    .line 46
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->getServiceIntent(Landroid/content/Context;)Landroid/content/Intent;

    move-result-object v2

    invoke-static {}, Lamirz/aidlbridge/LauncherClientIntent;->getPackage()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

    iget v3, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->flags:I

    iget-object v4, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->bridge:Landroid/content/ServiceConnection;

    invoke-virtual {v1, v2, v4, v3}, Landroid/content/Context;->bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z

    move-result v1

    iput-boolean v1, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->boundSuccessfully:Z
    :try_end_14
    .catch Ljava/lang/SecurityException; {:try_start_4 .. :try_end_14} :catch_17

    .line 51
    :cond_14
    :goto_14
    iget-boolean v1, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->boundSuccessfully:Z

    return v1

    .line 47
    :catch_17
    move-exception v0

    .line 48
    .local v0, "e":Ljava/lang/SecurityException;
    const-string v1, "LauncherClient"

    const-string v2, "Unable to connect to overlay service"

    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_14
.end method

.method public isBound()Z
    .registers 2

    .prologue
    .line 36
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->boundSuccessfully:Z

    return v0
.end method

.method public onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .registers 3
    .param p1, "componentName"    # Landroid/content/ComponentName;
    .param p2, "iBinder"    # Landroid/os/IBinder;

    .prologue
    .line 23
    return-void
.end method

.method public onServiceDisconnected(Landroid/content/ComponentName;)V
    .registers 2
    .param p1, "componentName"    # Landroid/content/ComponentName;

    .prologue
    .line 26
    return-void
.end method

.method public unbindSelf()V
    .registers 3

    .prologue
    .line 29
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->boundSuccessfully:Z

    if-eqz v0, :cond_c

    .line 30
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->context:Landroid/content/Context;

    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->bridge:Landroid/content/ServiceConnection;

    invoke-virtual {v0, v1}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    .line 31
    const/4 v0, 0x0

    iput-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->boundSuccessfully:Z

    .line 33
    :cond_c
    return-void
.end method
