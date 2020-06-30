.class public Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
.super Ljava/lang/Object;
.source "LauncherClient.java"


# annotations
.annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
.end annotation

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;,
        Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;
    }
.end annotation


# static fields
.field private static final DEBUG:Z = false

.field private static final EXTRA_SERVICE_VERSION:Ljava/lang/String; = "service.api.version"

.field private static final HIDE_WINDOW_WHEN_OVERLAY_OPEN:Z = false

.field private static final MIN_SERVICE_VERSION_FOR_ATTACH2:I = 0x3

.field private static final TAG:Ljava/lang/String; = "DrawerOverlayClient"

.field private static serviceVersion:I


# instance fields
.field private final activity:Landroid/app/Activity;

.field private final activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

.field private activityState:I

.field private final appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

.field private final clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

.field private currentServiceConnectionOptions:I

.field private destroyed:Z

.field private final launcherClientCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;

.field protected mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

.field private overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

.field private privateOptions:Landroid/os/Bundle;

.field private final serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

.field private serviceStatus:I

.field private final updateReceiver:Landroid/content/BroadcastReceiver;

.field private windowAttrs:Landroid/view/WindowManager$LayoutParams;


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 137
    const/4 v0, -0x1

    sput v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    return-void
.end method

.method public constructor <init>(Landroid/app/Activity;)V
    .registers 3
    .param p1, "activity"    # Landroid/app/Activity;
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 183
    new-instance v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacksAdapter;

    invoke-direct {v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacksAdapter;-><init>()V

    invoke-direct {p0, p1, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;-><init>(Landroid/app/Activity;Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;)V

    .line 184
    return-void
.end method

.method public constructor <init>(Landroid/app/Activity;Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;)V
    .registers 5
    .param p1, "activity"    # Landroid/app/Activity;
    .param p2, "callbacks"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    const/4 v1, 0x1

    .line 196
    new-instance v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;

    invoke-direct {v0, v1, v1, v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;-><init>(ZZZ)V

    invoke-direct {p0, p1, p2, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;-><init>(Landroid/app/Activity;Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)V

    .line 197
    return-void
.end method

.method public constructor <init>(Landroid/app/Activity;Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)V
    .registers 10
    .param p1, "activity"    # Landroid/app/Activity;
    .param p2, "callbacks"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;
    .param p3, "clientOptions"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    const/16 v5, 0x13

    const/4 v4, 0x0

    .line 209
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 144
    new-instance v1, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v2, "Client"

    const/16 v3, 0x14

    invoke-direct {v1, v2, v3}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;-><init>(Ljava/lang/String;I)V

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    .line 145
    new-instance v1, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v2, "Service"

    const/16 v3, 0xa

    invoke-direct {v1, v2, v3}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;-><init>(Ljava/lang/String;I)V

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    .line 150
    new-instance v1, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;

    invoke-direct {v1, p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;-><init>(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)V

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->updateReceiver:Landroid/content/BroadcastReceiver;

    .line 170
    iput v4, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    .line 171
    iput-boolean v4, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    .line 173
    iput v4, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceStatus:I

    .line 210
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    .line 211
    iput-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->launcherClientCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;

    .line 213
    new-instance v1, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    const/16 v2, 0x41

    invoke-direct {v1, p1, v2}, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;-><init>(Landroid/content/Context;I)V

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    .line 215
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->options:I
    invoke-static {p3}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->access$400(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)I

    move-result v1

    iput v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->currentServiceConnectionOptions:I

    .line 217
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->get(Landroid/content/Context;)Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    move-result-object v1

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    .line 218
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    invoke-virtual {v1, p0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->setClient(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    move-result-object v1

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    .line 221
    new-instance v0, Landroid/content/IntentFilter;

    const-string v1, "android.intent.action.PACKAGE_ADDED"

    invoke-direct {v0, v1}, Landroid/content/IntentFilter;-><init>(Ljava/lang/String;)V

    .line 222
    .local v0, "filter":Landroid/content/IntentFilter;
    const-string v1, "package"

    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addDataScheme(Ljava/lang/String;)V

    .line 223
    sget v1, Landroid/os/Build$VERSION;->SDK_INT:I

    if-lt v1, v5, :cond_5f

    .line 224
    const-string v1, "com.google.android.googlequicksearchbox"

    invoke-virtual {v0, v1, v4}, Landroid/content/IntentFilter;->addDataSchemeSpecificPart(Ljava/lang/String;I)V

    .line 226
    :cond_5f
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->updateReceiver:Landroid/content/BroadcastReceiver;

    invoke-virtual {v1, v2, v0}, Landroid/app/Activity;->registerReceiver(Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;)Landroid/content/Intent;

    .line 227
    sget v1, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    const/4 v2, 0x1

    if-ge v1, v2, :cond_6e

    .line 228
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reloadServiceVersion(Landroid/content/Context;)V

    .line 232
    :cond_6e
    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reconnect()V

    .line 238
    sget v1, Landroid/os/Build$VERSION;->SDK_INT:I

    if-lt v1, v5, :cond_9c

    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    .line 239
    invoke-virtual {v1}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    move-result-object v1

    if-eqz v1, :cond_9c

    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    .line 240
    invoke-virtual {v1}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    move-result-object v1

    invoke-virtual {v1}, Landroid/view/Window;->peekDecorView()Landroid/view/View;

    move-result-object v1

    if-eqz v1, :cond_9c

    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    .line 241
    invoke-virtual {v1}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    move-result-object v1

    invoke-virtual {v1}, Landroid/view/Window;->peekDecorView()Landroid/view/View;

    move-result-object v1

    invoke-virtual {v1}, Landroid/view/View;->isAttachedToWindow()Z

    move-result v1

    if-eqz v1, :cond_9c

    .line 242
    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->onAttachedToWindow()V

    .line 244
    :cond_9c
    return-void
.end method

.method static synthetic access$000(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 102
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    return-object v0
.end method

.method static synthetic access$100(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 102
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    return-object v0
.end method

.method static synthetic access$200(Landroid/content/Context;)V
    .registers 1
    .param p0, "x0"    # Landroid/content/Context;

    .prologue
    .line 102
    invoke-static {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reloadServiceVersion(Landroid/content/Context;)V

    return-void
.end method

.method static synthetic access$300(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)I
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 102
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    return v0
.end method

.method static synthetic access$500(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;I)V
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
    .param p1, "x1"    # I

    .prologue
    .line 102
    invoke-direct {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->notifyStatusChanged(I)V

    return-void
.end method

.method static synthetic access$600(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Landroid/app/Activity;
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 102
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    return-object v0
.end method

.method static synthetic access$700(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)I
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 102
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceStatus:I

    return v0
.end method

.method static synthetic access$800(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 102
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->launcherClientCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;

    return-object v0
.end method

.method static synthetic access$900(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 102
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    return-object v0
.end method

.method private applyWindowToken()V
    .registers 6

    .prologue
    .line 470
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v1, :cond_34

    .line 472
    :try_start_4
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    if-nez v1, :cond_f

    .line 473
    new-instance v1, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    invoke-direct {v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;-><init>()V

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    .line 475
    :cond_f
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    invoke-virtual {v1, p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->setClient(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)V

    .line 476
    sget v1, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    const/4 v2, 0x3

    if-ge v1, v2, :cond_35

    .line 477
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    iget-object v3, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    iget v4, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->currentServiceConnectionOptions:I

    invoke-interface {v1, v2, v3, v4}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->windowAttached(Landroid/view/WindowManager$LayoutParams;Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;I)V

    .line 488
    :goto_24
    sget v1, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    const/4 v2, 0x4

    if-ge v1, v2, :cond_70

    .line 489
    iget v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    and-int/lit8 v1, v1, 0x2

    if-eqz v1, :cond_6a

    .line 490
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->onResume()V

    .line 503
    :cond_34
    :goto_34
    return-void

    .line 479
    :cond_35
    new-instance v0, Landroid/os/Bundle;

    invoke-direct {v0}, Landroid/os/Bundle;-><init>()V

    .line 480
    .local v0, "params":Landroid/os/Bundle;
    const-string v1, "layout_params"

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    invoke-virtual {v0, v1, v2}, Landroid/os/Bundle;->putParcelable(Ljava/lang/String;Landroid/os/Parcelable;)V

    .line 481
    const-string v1, "configuration"

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    invoke-virtual {v2}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual {v2}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v2

    invoke-virtual {v0, v1, v2}, Landroid/os/Bundle;->putParcelable(Ljava/lang/String;Landroid/os/Parcelable;)V

    .line 482
    const-string v1, "client_options"

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->currentServiceConnectionOptions:I

    invoke-virtual {v0, v1, v2}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 483
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->privateOptions:Landroid/os/Bundle;

    if-eqz v1, :cond_60

    .line 484
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->privateOptions:Landroid/os/Bundle;

    invoke-virtual {v0, v1}, Landroid/os/Bundle;->putAll(Landroid/os/Bundle;)V

    .line 486
    :cond_60
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    invoke-interface {v1, v0, v2}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->windowAttached2(Landroid/os/Bundle;Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;)V

    goto :goto_24

    .line 497
    .end local v0    # "params":Landroid/os/Bundle;
    :catch_68
    move-exception v1

    goto :goto_34

    .line 492
    :cond_6a
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->onPause()V

    goto :goto_34

    .line 495
    :cond_70
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-interface {v1, v2}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->setActivityState(I)V
    :try_end_77
    .catch Landroid/os/RemoteException; {:try_start_4 .. :try_end_77} :catch_68

    goto :goto_34
.end method

.method static getServiceIntent(Landroid/content/Context;)Landroid/content/Intent;
    .registers 6
    .param p0, "context"    # Landroid/content/Context;

    .prologue
    .line 786
    .line 787
    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v1

    invoke-static {}, Landroid/os/Process;->myUid()I

    move-result v2

    invoke-static {v1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/String;->length()I

    move-result v3

    add-int/lit8 v3, v3, 0x12

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4, v3}, Ljava/lang/StringBuilder;-><init>(I)V

    const-string v3, "app://"

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v3, ":"

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v1}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;

    move-result-object v1

    .line 788
    invoke-virtual {v1}, Landroid/net/Uri;->buildUpon()Landroid/net/Uri$Builder;

    move-result-object v1

    const-string v2, "v"

    const/16 v3, 0x9

    .line 789
    invoke-static {v3}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v2, v3}, Landroid/net/Uri$Builder;->appendQueryParameter(Ljava/lang/String;Ljava/lang/String;)Landroid/net/Uri$Builder;

    move-result-object v1

    const-string v2, "cv"

    const/16 v3, 0xe

    .line 790
    invoke-static {v3}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v2, v3}, Landroid/net/Uri$Builder;->appendQueryParameter(Ljava/lang/String;Ljava/lang/String;)Landroid/net/Uri$Builder;

    move-result-object v1

    .line 791
    invoke-virtual {v1}, Landroid/net/Uri$Builder;->build()Landroid/net/Uri;

    move-result-object v0

    .line 792
    .local v0, "uri":Landroid/net/Uri;
    new-instance v1, Landroid/content/Intent;

    const-string v2, "com.android.launcher3.WINDOW_OVERLAY"

    invoke-direct {v1, v2}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const-string v2, "com.google.android.googlequicksearchbox"

    invoke-virtual {v1, v2}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object v1

    invoke-virtual {v1, v0}, Landroid/content/Intent;->setData(Landroid/net/Uri;)Landroid/content/Intent;

    move-result-object v1

    return-object v1
.end method

.method private isConnected()Z
    .registers 2

    .prologue
    .line 506
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_6

    const/4 v0, 0x1

    :goto_5
    return v0

    :cond_6
    const/4 v0, 0x0

    goto :goto_5
.end method

.method private notifyStatusChanged(I)V
    .registers 7
    .param p1, "status"    # I

    .prologue
    const/4 v0, 0x1

    const/4 v1, 0x0

    .line 751
    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceStatus:I

    if-eq v2, p1, :cond_16

    .line 752
    iput p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceStatus:I

    .line 753
    iget-object v3, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->launcherClientCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;

    and-int/lit8 v2, p1, 0x1

    if-eqz v2, :cond_17

    move v2, v0

    :goto_f
    and-int/lit8 v4, p1, 0x2

    if-eqz v4, :cond_19

    :goto_13
    invoke-interface {v3, v2, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;->onServiceStateChanged(ZZ)V

    .line 757
    :cond_16
    return-void

    :cond_17
    move v2, v1

    .line 753
    goto :goto_f

    :cond_19
    move v0, v1

    goto :goto_13
.end method

.method private reattachOverlayInternal()V
    .registers 3

    .prologue
    .line 729
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_c

    sget v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    const/4 v1, 0x7

    if-lt v0, v1, :cond_c

    .line 730
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->applyWindowToken()V

    .line 732
    :cond_c
    return-void
.end method

.method private static reloadServiceVersion(Landroid/content/Context;)V
    .registers 3
    .param p0, "context"    # Landroid/content/Context;

    .prologue

    .line 899
    .line 901
    invoke-virtual {p0}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v0

    .line 902
    invoke-static {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->getServiceIntent(Landroid/content/Context;)Landroid/content/Intent;

    move-result-object v1

    invoke-static {v0, v1}, Lamirz/aidlbridge/LauncherClientIntent;->getServiceVersion(Landroid/content/pm/PackageManager;Landroid/content/Intent;)I

    move-result-object v0

    sput v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    return-void
.end method

.method private removeClient(Z)V
    .registers 4
    .param p1, "unbindApp"    # Z

    .prologue
    .line 416
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-nez v0, :cond_b

    .line 417
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->updateReceiver:Landroid/content/BroadcastReceiver;

    invoke-virtual {v0, v1}, Landroid/app/Activity;->unregisterReceiver(Landroid/content/BroadcastReceiver;)V

    .line 419
    :cond_b
    const/4 v0, 0x1

    iput-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    .line 420
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    invoke-virtual {v0}, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->unbindSelf()V

    .line 421
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    if-eqz v0, :cond_1f

    .line 422
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    invoke-virtual {v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->clear()V

    .line 423
    const/4 v0, 0x0

    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->overlayCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;

    .line 425
    :cond_1f
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    invoke-virtual {v0, p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->clearClientIfSame(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;Z)V

    .line 426
    return-void
.end method

.method private setWindowAttrs(Landroid/view/WindowManager$LayoutParams;)V
    .registers 4
    .param p1, "windowAttrs"    # Landroid/view/WindowManager$LayoutParams;

    .prologue
    .line 451
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-ne v0, p1, :cond_5

    .line 467
    :cond_4
    :goto_4
    return-void

    .line 454
    :cond_5
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    .line 455
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_f

    .line 456
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->applyWindowToken()V

    goto :goto_4

    .line 457
    :cond_f
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_4

    .line 459
    :try_start_13
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    invoke-virtual {v1}, Landroid/app/Activity;->isChangingConfigurations()Z

    move-result v1

    invoke-interface {v0, v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->windowDetached(Z)V
    :try_end_1e
    .catch Landroid/os/RemoteException; {:try_start_13 .. :try_end_1e} :catch_22

    .line 465
    :goto_1e
    const/4 v0, 0x0

    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    goto :goto_4

    .line 460
    :catch_22
    move-exception v0

    goto :goto_1e
.end method

.method private verifyAndGetAnimationFlags(I)I
    .registers 4
    .param p1, "duration"    # I

    .prologue
    .line 572
    if-lez p1, :cond_6

    const/16 v0, 0x7ff

    if-le p1, v0, :cond_e

    .line 573
    :cond_6
    new-instance v0, Ljava/lang/IllegalArgumentException;

    const-string v1, "Invalid duration"

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 575
    :cond_e
    shl-int/lit8 v0, p1, 0x2

    or-int/lit8 v0, v0, 0x1

    return v0
.end method


# virtual methods
.method public disconnect()V
    .registers 2
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 396
    const/4 v0, 0x1

    invoke-direct {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->removeClient(Z)V

    .line 397
    return-void
.end method

.method public dump(Ljava/lang/String;Ljava/io/PrintWriter;)V
    .registers 6
    .param p1, "prefix"    # Ljava/lang/String;
    .param p2, "w"    # Ljava/io/PrintWriter;
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 768
    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    const-string v1, "LauncherClient"

    invoke-virtual {v0, v1}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 769
    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    const-string v1, "  "

    invoke-virtual {v0, v1}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 771
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->isConnected()Z

    move-result v0

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    add-int/lit8 v1, v1, 0x12

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "isConnected: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 772
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    invoke-virtual {v0}, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->isBound()Z

    move-result v0

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    add-int/lit8 v1, v1, 0x12

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "act.isBound: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 773
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    invoke-virtual {v0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->isBound()Z

    move-result v0

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    add-int/lit8 v1, v1, 0x12

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "app.isBound: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 774
    sget v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    add-int/lit8 v1, v1, 0x1b

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "serviceVersion: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 775
    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v0

    add-int/lit8 v0, v0, 0x11

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1, v0}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, "clientVersion: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const/16 v1, 0xe

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 777
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    add-int/lit8 v1, v1, 0x1b

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "mActivityState: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 778
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceStatus:I

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    add-int/lit8 v1, v1, 0x1b

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "mServiceStatus: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 779
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->currentServiceConnectionOptions:I

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    add-int/lit8 v1, v1, 0x2d

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "mCurrentServiceConnectionOptions: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 780
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    invoke-virtual {v0, p1, p2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->dump(Ljava/lang/String;Ljava/io/PrintWriter;)V

    .line 781
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    invoke-virtual {v0, p1, p2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->dump(Ljava/lang/String;Ljava/io/PrintWriter;)V

    .line 782
    return-void
.end method

.method public endMove()V
    .registers 3
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 536
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "endMove"

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .line 537
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->isConnected()Z

    move-result v0

    if-eqz v0, :cond_12

    .line 539
    :try_start_d
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->endScroll()V
    :try_end_12
    .catch Landroid/os/RemoteException; {:try_start_d .. :try_end_12} :catch_13

    .line 546
    :cond_12
    :goto_12
    return-void

    .line 540
    :catch_13
    move-exception v0

    goto :goto_12
.end method

.method public hideOverlay(I)V
    .registers 5
    .param p1, "duration"    # I
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 606
    invoke-direct {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->verifyAndGetAnimationFlags(I)I

    move-result v0

    .line 607
    .local v0, "flags":I
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v2, "hideOverlay"

    invoke-virtual {v1, v2, p1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    .line 608
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v1, :cond_14

    .line 610
    :try_start_f
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v1, v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->closeOverlay(I)V
    :try_end_14
    .catch Landroid/os/RemoteException; {:try_start_f .. :try_end_14} :catch_15

    .line 617
    :cond_14
    :goto_14
    return-void

    .line 611
    :catch_15
    move-exception v1

    goto :goto_14
.end method

.method public hideOverlay(Z)V
    .registers 4
    .param p1, "animate"    # Z
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 585
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "hideOverlay"

    invoke-virtual {v0, v1, p1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;Z)V

    .line 586
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_13

    .line 588
    :try_start_b
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    .line 589
    if-eqz p1, :cond_14

    const/4 v0, 0x1

    .line 588
    :goto_10
    invoke-interface {v1, v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->closeOverlay(I)V
    :try_end_13
    .catch Landroid/os/RemoteException; {:try_start_b .. :try_end_13} :catch_16

    .line 596
    :cond_13
    :goto_13
    return-void

    .line 589
    :cond_14
    const/4 v0, 0x0

    goto :goto_10

    .line 590
    :catch_16
    move-exception v0

    goto :goto_13
.end method

.method public final onAttachedToWindow()V
    .registers 3
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 252
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-eqz v0, :cond_5

    .line 257
    :goto_4
    return-void

    .line 255
    :cond_5
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "attachedToWindow"

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .line 256
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    move-result-object v0

    invoke-virtual {v0}, Landroid/view/Window;->getAttributes()Landroid/view/WindowManager$LayoutParams;

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->setWindowAttrs(Landroid/view/WindowManager$LayoutParams;)V

    goto :goto_4
.end method

.method public onDestroy()V
    .registers 2
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 387
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->isChangingConfigurations()Z

    move-result v0

    if-nez v0, :cond_d

    const/4 v0, 0x1

    :goto_9
    invoke-direct {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->removeClient(Z)V

    .line 388
    return-void

    .line 387
    :cond_d
    const/4 v0, 0x0

    goto :goto_9
.end method

.method public final onDetachedFromWindow()V
    .registers 3
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 265
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-eqz v0, :cond_5

    .line 270
    :goto_4
    return-void

    .line 268
    :cond_5
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "detachedFromWindow"

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .line 269
    const/4 v0, 0x0

    invoke-direct {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->setWindowAttrs(Landroid/view/WindowManager$LayoutParams;)V

    goto :goto_4
.end method

.method public onPause()V
    .registers 4
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 305
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-eqz v0, :cond_5

    .line 324
    :goto_4
    return-void

    .line 309
    :cond_5
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    and-int/lit8 v0, v0, -0x3

    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    .line 310
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_1d

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_1d

    .line 312
    :try_start_13
    sget v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    const/4 v1, 0x4

    if-ge v0, v1, :cond_27

    .line 313
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->onPause()V
    :try_end_1d
    .catch Landroid/os/RemoteException; {:try_start_13 .. :try_end_1d} :catch_2f

    .line 323
    :cond_1d
    :goto_1d
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "stateChanged "

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-virtual {v0, v1, v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    goto :goto_4

    .line 315
    :cond_27
    :try_start_27
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-interface {v0, v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->setActivityState(I)V
    :try_end_2e
    .catch Landroid/os/RemoteException; {:try_start_27 .. :try_end_2e} :catch_2f

    goto :goto_1d

    .line 317
    :catch_2f
    move-exception v0

    goto :goto_1d
.end method

.method public onResume()V
    .registers 4
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 278
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-eqz v0, :cond_5

    .line 297
    :goto_4
    return-void

    .line 282
    :cond_5
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    or-int/lit8 v0, v0, 0x2

    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    .line 283
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_1d

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_1d

    .line 285
    :try_start_13
    sget v0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    const/4 v1, 0x4

    if-ge v0, v1, :cond_27

    .line 286
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->onResume()V
    :try_end_1d
    .catch Landroid/os/RemoteException; {:try_start_13 .. :try_end_1d} :catch_2f

    .line 296
    :cond_1d
    :goto_1d
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "stateChanged "

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-virtual {v0, v1, v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    goto :goto_4

    .line 288
    :cond_27
    :try_start_27
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-interface {v0, v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->setActivityState(I)V
    :try_end_2e
    .catch Landroid/os/RemoteException; {:try_start_27 .. :try_end_2e} :catch_2f

    goto :goto_1d

    .line 290
    :catch_2f
    move-exception v0

    goto :goto_1d
.end method

.method public onStart()V
    .registers 4
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 332
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-eqz v0, :cond_5

    .line 350
    :goto_4
    return-void

    .line 336
    :cond_5
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->setAutoUnbind(Z)V

    .line 337
    invoke-virtual {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reconnect()V

    .line 339
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    or-int/lit8 v0, v0, 0x1

    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    .line 340
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_23

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_23

    .line 342
    :try_start_1c
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-interface {v0, v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->setActivityState(I)V
    :try_end_23
    .catch Landroid/os/RemoteException; {:try_start_1c .. :try_end_23} :catch_2d

    .line 349
    :cond_23
    :goto_23
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "stateChanged "

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-virtual {v0, v1, v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    goto :goto_4

    .line 343
    :catch_2d
    move-exception v0

    goto :goto_23
.end method

.method public onStop()V
    .registers 4
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 358
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-eqz v0, :cond_5

    .line 378
    :goto_4
    return-void

    .line 363
    :cond_5
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->setAutoUnbind(Z)V

    .line 365
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    invoke-virtual {v0}, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->unbindSelf()V

    .line 367
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    and-int/lit8 v0, v0, -0x2

    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    .line 368
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_25

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_25

    .line 370
    :try_start_1e
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    iget v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-interface {v0, v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->setActivityState(I)V
    :try_end_25
    .catch Landroid/os/RemoteException; {:try_start_1e .. :try_end_25} :catch_2f

    .line 377
    :cond_25
    :goto_25
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "stateChanged "

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I

    invoke-virtual {v0, v1, v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    goto :goto_4

    .line 371
    :catch_2f
    move-exception v0

    goto :goto_25
.end method

.method overlayAvailabilityChanged(Lcom/google/android/libraries/launcherclient/ILauncherOverlay;)V
    .registers 6
    .param p1, "overlay"    # Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    .prologue
    const/4 v1, 0x0

    .line 736
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v3, "Connected"

    if-eqz p1, :cond_15

    const/4 v0, 0x1

    :goto_8
    invoke-virtual {v2, v3, v0}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;Z)V

    .line 740
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    .line 741
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-nez v0, :cond_17

    .line 742
    invoke-direct {p0, v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->notifyStatusChanged(I)V

    .line 748
    :cond_14
    :goto_14
    return-void

    :cond_15
    move v0, v1

    .line 736
    goto :goto_8

    .line 744
    :cond_17
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_14

    .line 745
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->applyWindowToken()V

    goto :goto_14
.end method

.method public reattachOverlay()V
    .registers 3
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 724
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "reattachOverlay"

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .line 725
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reattachOverlayInternal()V

    .line 726
    return-void
.end method

.method public reconnect()V
    .registers 3
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 434
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->destroyed:Z

    if-eqz v0, :cond_5

    .line 448
    :cond_4
    :goto_4
    return-void

    .line 437
    :cond_5
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    invoke-virtual {v0}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->connectSafely()Z

    move-result v0

    if-eqz v0, :cond_15

    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    invoke-virtual {v0}, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->connectSafely()Z

    move-result v0

    if-nez v0, :cond_4

    .line 440
    :cond_15
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;

    new-instance v1, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$2;

    invoke-direct {v1, p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$2;-><init>(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)V

    invoke-virtual {v0, v1}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    goto :goto_4
.end method

.method public requestHotwordDetection(Z)V
    .registers 4
    .param p1, "start"    # Z
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 691
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "requestHotwordDetection"

    invoke-virtual {v0, v1, p1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;Z)V

    .line 692
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_10

    .line 694
    :try_start_b
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v0, p1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->requestVoiceDetection(Z)V
    :try_end_10
    .catch Landroid/os/RemoteException; {:try_start_b .. :try_end_10} :catch_11

    .line 701
    :cond_10
    :goto_10
    return-void

    .line 695
    :catch_11
    move-exception v0

    goto :goto_10
.end method

.method public setClientOptions(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)V
    .registers 5
    .param p1, "clientOptions"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 406
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->options:I
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->access$400(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)I

    move-result v0

    iget v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->currentServiceConnectionOptions:I

    if-eq v0, v1, :cond_1e

    .line 407
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->options:I
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->access$400(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)I

    move-result v0

    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->currentServiceConnectionOptions:I

    .line 408
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->windowAttrs:Landroid/view/WindowManager$LayoutParams;

    if-eqz v0, :cond_15

    .line 409
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->applyWindowToken()V

    .line 411
    :cond_15
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "setClientOptions "

    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->currentServiceConnectionOptions:I

    invoke-virtual {v0, v1, v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    .line 413
    :cond_1e
    return-void
.end method

.method public setPrivateOptions(Landroid/os/Bundle;)V
    .registers 6
    .param p1, "options"    # Landroid/os/Bundle;

    .prologue
    .line 709
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v2, "setPrivateOptions : "

    .line 711
    if-nez p1, :cond_1f

    const-string v0, "null"

    :goto_8
    invoke-static {v0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v3

    if-eqz v3, :cond_2a

    invoke-virtual {v2, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 709
    :goto_16
    invoke-virtual {v1, v0}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .line 712
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->privateOptions:Landroid/os/Bundle;

    .line 713
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reattachOverlayInternal()V

    .line 714
    return-void

    .line 711
    :cond_1f
    const-string v0, ","

    invoke-virtual {p1}, Landroid/os/Bundle;->keySet()Ljava/util/Set;

    move-result-object v3

    invoke-static {v0, v3}, Landroid/text/TextUtils;->join(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;

    move-result-object v0

    goto :goto_8

    :cond_2a
    new-instance v0, Ljava/lang/String;

    invoke-direct {v0, v2}, Ljava/lang/String;-><init>(Ljava/lang/String;)V

    goto :goto_16
.end method

.method public showOverlay(I)V
    .registers 5
    .param p1, "duration"    # I
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 646
    invoke-direct {p0, p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->verifyAndGetAnimationFlags(I)I

    move-result v0

    .line 647
    .local v0, "flags":I
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v2, "showOverlay"

    invoke-virtual {v1, v2, p1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    .line 648
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v1, :cond_14

    .line 650
    :try_start_f
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v1, v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->openOverlay(I)V
    :try_end_14
    .catch Landroid/os/RemoteException; {:try_start_f .. :try_end_14} :catch_15

    .line 657
    :cond_14
    :goto_14
    return-void

    .line 651
    :catch_15
    move-exception v1

    goto :goto_14
.end method

.method public showOverlay(Z)V
    .registers 4
    .param p1, "animate"    # Z
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 626
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "showOverlay"

    invoke-virtual {v0, v1, p1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;Z)V

    .line 627
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v0, :cond_13

    .line 629
    :try_start_b
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz p1, :cond_14

    const/4 v0, 0x1

    :goto_10
    invoke-interface {v1, v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->openOverlay(I)V
    :try_end_13
    .catch Landroid/os/RemoteException; {:try_start_b .. :try_end_13} :catch_16

    .line 636
    :cond_13
    :goto_13
    return-void

    .line 629
    :cond_14
    const/4 v0, 0x0

    goto :goto_10

    .line 630
    :catch_16
    move-exception v0

    goto :goto_13
.end method

.method public startMove()V
    .registers 3
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 516
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "startMove"

    invoke-virtual {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .line 517
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->isConnected()Z

    move-result v0

    if-eqz v0, :cond_12

    .line 519
    :try_start_d
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->startScroll()V
    :try_end_12
    .catch Landroid/os/RemoteException; {:try_start_d .. :try_end_12} :catch_13

    .line 526
    :cond_12
    :goto_12
    return-void

    .line 520
    :catch_13
    move-exception v0

    goto :goto_12
.end method

.method public startSearch([BLandroid/os/Bundle;)Z
    .registers 7
    .param p1, "config"    # [B
    .param p2, "extras"    # Landroid/os/Bundle;

    .prologue
    const/4 v1, 0x0

    .line 670
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v3, "startSearch"

    invoke-virtual {v2, v3}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .line 671
    sget v2, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceVersion:I

    const/4 v3, 0x6

    if-ge v2, v3, :cond_e

    .line 681
    :cond_d
    :goto_d
    return v1

    .line 674
    :cond_e
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v2, :cond_d

    .line 676
    :try_start_12
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v2, p1, p2}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->startSearch([BLandroid/os/Bundle;)Z
    :try_end_17
    .catch Landroid/os/RemoteException; {:try_start_12 .. :try_end_17} :catch_19

    move-result v1

    goto :goto_d

    .line 677
    :catch_19
    move-exception v0

    .line 678
    .local v0, "e":Landroid/os/RemoteException;
    const-string v2, "DrawerOverlayClient"

    const-string v3, "Error starting session for search"

    invoke-static {v2, v3, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_d
.end method

.method public updateMove(F)V
    .registers 4
    .param p1, "progressX"    # F
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 559
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->clientEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    const-string v1, "updateMove"

    invoke-virtual {v0, v1, p1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;F)V

    .line 560
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->isConnected()Z

    move-result v0

    if-eqz v0, :cond_12

    .line 562
    :try_start_d
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->mOverlay:Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    invoke-interface {v0, p1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;->onScroll(F)V
    :try_end_12
    .catch Landroid/os/RemoteException; {:try_start_d .. :try_end_12} :catch_13

    .line 569
    :cond_12
    :goto_12
    return-void

    .line 563
    :catch_13
    move-exception v0

    goto :goto_12
.end method
