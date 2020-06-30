.class Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;
.super Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;
.source "AppServiceConnection.java"


# static fields
.field private static instance:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;


# instance fields
.field private activeClient:Ljava/lang/ref/WeakReference;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/lang/ref/WeakReference",
            "<",
            "Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;",
            ">;"
        }
    .end annotation
.end field

.field private autoUnbind:Z

.field private overlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;


# direct methods
.method private constructor <init>(Landroid/content/Context;)V
    .registers 3
    .param p1, "context"    # Landroid/content/Context;

    .prologue
    .line 27
    const/16 v0, 0x21

    invoke-direct {p0, p1, v0}, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;-><init>(Landroid/content/Context;I)V

    .line 28
    return-void
.end method

.method static get(Landroid/content/Context;)Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;
    .registers 3
    .param p0, "context"    # Landroid/content/Context;

    .prologue
    .line 15
    sget-object v0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->instance:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    if-nez v0, :cond_f

    .line 16
    new-instance v0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    invoke-virtual {p0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;

    move-result-object v1

    invoke-direct {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;-><init>(Landroid/content/Context;)V

    sput-object v0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->instance:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    .line 18
    :cond_f
    sget-object v0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->instance:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    return-object v0
.end method

.method private getBoundClient()Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
    .registers 2

    .prologue
    .line 87
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->activeClient:Ljava/lang/ref/WeakReference;

    if-eqz v0, :cond_d

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->activeClient:Ljava/lang/ref/WeakReference;

    invoke-virtual {v0}, Ljava/lang/ref/WeakReference;->get()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    :goto_c
    return-object v0

    :cond_d
    const/4 v0, 0x0

    goto :goto_c
.end method

.method private setOverlay(Lcom/google/android/libraries/launcherclient/ILauncherOverlay;)V
    .registers 4
    .param p1, "overlay"    # Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    .prologue
    .line 78
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->overlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    .line 80
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->getBoundClient()Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    move-result-object v0

    .line 81
    .local v0, "client":Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
    if-eqz v0, :cond_d

    .line 82
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->overlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayAvailabilityChanged(Lcom/google/android/libraries/launcherclient/ILauncherOverlay;)V

    .line 84
    :cond_d
    return-void
.end method

.method private unbindIfNeeded()V
    .registers 2

    .prologue
    .line 72
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->autoUnbind:Z

    if-eqz v0, :cond_b

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->overlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-nez v0, :cond_b

    .line 73
    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->unbindSelf()V

    .line 75
    :cond_b
    return-void
.end method


# virtual methods
.method public clearClientIfSame(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;Z)V
    .registers 6
    .param p1, "client"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
    .param p2, "unbind"    # Z

    .prologue
    const/4 v2, 0x0

    .line 47
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->getBoundClient()Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    move-result-object v0

    .line 48
    .local v0, "boundClient":Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
    if-eqz v0, :cond_1a

    invoke-virtual {v0, p1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_1a

    .line 49
    iput-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->activeClient:Ljava/lang/ref/WeakReference;

    .line 51
    if-eqz p2, :cond_1a

    .line 52
    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->unbindSelf()V

    .line 53
    sget-object v1, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->instance:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    if-ne v1, p0, :cond_1a

    .line 54
    sput-object v2, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->instance:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    .line 58
    :cond_1a
    return-void
.end method

.method public onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .registers 4
    .param p1, "componentName"    # Landroid/content/ComponentName;
    .param p2, "iBinder"    # Landroid/os/IBinder;

    .prologue
    .line 62
    invoke-static {p2}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->asInterface(Landroid/os/IBinder;)Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->setOverlay(Lcom/google/android/libraries/launcherclient/ILauncherOverlay;)V

    .line 63
    return-void
.end method

.method public onServiceDisconnected(Landroid/content/ComponentName;)V
    .registers 3
    .param p1, "componentName"    # Landroid/content/ComponentName;

    .prologue
    .line 67
    const/4 v0, 0x0

    invoke-direct {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->setOverlay(Lcom/google/android/libraries/launcherclient/ILauncherOverlay;)V

    .line 68
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->unbindIfNeeded()V

    .line 69
    return-void
.end method

.method public setAutoUnbind(Z)V
    .registers 2
    .param p1, "canAutoUnbind"    # Z

    .prologue
    .line 42
    iput-boolean p1, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->autoUnbind:Z

    .line 43
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->unbindIfNeeded()V

    .line 44
    return-void
.end method

.method public setClient(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/launcherclient/ILauncherOverlay;
    .registers 3
    .param p1, "client"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 36
    new-instance v0, Ljava/lang/ref/WeakReference;

    invoke-direct {v0, p1}, Ljava/lang/ref/WeakReference;-><init>(Ljava/lang/Object;)V

    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->activeClient:Ljava/lang/ref/WeakReference;

    .line 37
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->overlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    return-object v0
.end method
