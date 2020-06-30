.class Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;
.super Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback$Stub;
.source "LauncherClient.java"

# interfaces
.implements Landroid/os/Handler$Callback;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0xa
    name = "OverlayCallbacks"
.end annotation


# static fields
.field private static final MSG_UPDATE_SCROLL:I = 0x2

.field private static final MSG_UPDATE_SHIFT:I = 0x3

.field private static final MSG_UPDATE_STATUS:I = 0x4


# instance fields
.field private client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

.field private final uIHandler:Landroid/os/Handler;

.field private window:Landroid/view/Window;

.field private windowHidden:Z

.field private windowManager:Landroid/view/WindowManager;

.field private windowShift:I


# direct methods
.method constructor <init>()V
    .registers 3

    .prologue
    .line 809
    invoke-direct {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback$Stub;-><init>()V

    .line 807
    const/4 v0, 0x0

    iput-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowHidden:Z

    .line 810
    new-instance v0, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1, p0}, Landroid/os/Handler;-><init>(Landroid/os/Looper;Landroid/os/Handler$Callback;)V

    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->uIHandler:Landroid/os/Handler;

    .line 811
    return-void
.end method

.method private hideActivityNonUI(Z)V
    .registers 3
    .param p1, "isHidden"    # Z

    .prologue
    .line 889
    iget-boolean v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowHidden:Z

    if-eq v0, p1, :cond_6

    .line 890
    iput-boolean p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowHidden:Z

    .line 895
    :cond_6
    return-void
.end method


# virtual methods
.method public clear()V
    .registers 2

    .prologue
    const/4 v0, 0x0

    .line 824
    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .line 825
    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowManager:Landroid/view/WindowManager;

    .line 826
    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->window:Landroid/view/Window;

    .line 827
    return-void
.end method

.method public handleMessage(Landroid/os/Message;)Z
    .registers 8
    .param p1, "msg"    # Landroid/os/Message;

    .prologue
    const/4 v4, 0x0

    const/4 v3, 0x1

    .line 845
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    if-nez v2, :cond_8

    move v2, v3

    .line 885
    :goto_7
    return v2

    .line 849
    :cond_8
    iget v2, p1, Landroid/os/Message;->what:I

    packed-switch v2, :pswitch_data_b8

    move v2, v4

    .line 885
    goto :goto_7

    .line 851
    :pswitch_f
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceStatus:I
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$700(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)I

    move-result v2

    and-int/lit8 v2, v2, 0x1

    if-eqz v2, :cond_3a

    .line 852
    iget-object v2, p1, Landroid/os/Message;->obj:Ljava/lang/Object;

    check-cast v2, Ljava/lang/Float;

    invoke-virtual {v2}, Ljava/lang/Float;->floatValue()F

    move-result v1

    .line 853
    .local v1, "scroll":F
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->launcherClientCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$800(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;

    move-result-object v2

    invoke-interface {v2, v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;->onOverlayScrollChanged(F)V

    .line 855
    const/4 v2, 0x0

    cmpg-float v2, v1, v2

    if-gtz v2, :cond_3c

    .line 856
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$900(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    move-result-object v2

    const-string v4, "onScroll 0, overlay closed"

    invoke-virtual {v2, v4}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    .end local v1    # "scroll":F
    :cond_3a
    :goto_3a
    move v2, v3

    .line 863
    goto :goto_7

    .line 857
    .restart local v1    # "scroll":F
    :cond_3c
    const/high16 v2, 0x3f800000    # 1.0f

    cmpl-float v2, v1, v2

    if-ltz v2, :cond_4e

    .line 858
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$900(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    move-result-object v2

    const-string v4, "onScroll 1, overlay opened"

    invoke-virtual {v2, v4}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;)V

    goto :goto_3a

    .line 860
    :cond_4e
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$900(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    move-result-object v2

    const-string v4, "onScroll"

    invoke-virtual {v2, v4, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;F)V

    goto :goto_3a

    .line 865
    .end local v1    # "scroll":F
    :pswitch_5a
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->window:Landroid/view/Window;

    invoke-virtual {v2}, Landroid/view/Window;->getAttributes()Landroid/view/WindowManager$LayoutParams;

    move-result-object v0

    .line 866
    .local v0, "attrs":Landroid/view/WindowManager$LayoutParams;
    iget-object v2, p1, Landroid/os/Message;->obj:Ljava/lang/Object;

    check-cast v2, Ljava/lang/Boolean;

    invoke-virtual {v2}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v2

    if-eqz v2, :cond_81

    .line 867
    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowShift:I

    iput v2, v0, Landroid/view/WindowManager$LayoutParams;->x:I

    .line 868
    iget v2, v0, Landroid/view/WindowManager$LayoutParams;->flags:I

    or-int/lit16 v2, v2, 0x200

    iput v2, v0, Landroid/view/WindowManager$LayoutParams;->flags:I

    .line 873
    :goto_74
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowManager:Landroid/view/WindowManager;

    iget-object v4, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->window:Landroid/view/Window;

    invoke-virtual {v4}, Landroid/view/Window;->getDecorView()Landroid/view/View;

    move-result-object v4

    invoke-interface {v2, v4, v0}, Landroid/view/WindowManager;->updateViewLayout(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    move v2, v3

    .line 874
    goto :goto_7

    .line 870
    :cond_81
    iput v4, v0, Landroid/view/WindowManager$LayoutParams;->x:I

    .line 871
    iget v2, v0, Landroid/view/WindowManager$LayoutParams;->flags:I

    and-int/lit16 v2, v2, -0x201

    iput v2, v0, Landroid/view/WindowManager$LayoutParams;->flags:I

    goto :goto_74

    .line 876
    .end local v0    # "attrs":Landroid/view/WindowManager$LayoutParams;
    :pswitch_8a
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    iget v4, p1, Landroid/os/Message;->arg1:I

    # invokes: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->notifyStatusChanged(I)V
    invoke-static {v2, v4}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$500(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;I)V

    .line 877
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->serviceEventLogArray:Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$900(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;

    move-result-object v2

    const-string v4, "stateChanged"

    iget v5, p1, Landroid/os/Message;->arg1:I

    invoke-virtual {v2, v4, v5}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(Ljava/lang/String;I)V

    .line 879
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->launcherClientCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$800(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;

    move-result-object v2

    instance-of v2, v2, Lcom/google/android/libraries/gsa/launcherclient/PrivateCallbacks;

    if-eqz v2, :cond_b5

    .line 880
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->launcherClientCallbacks:Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$800(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;

    move-result-object v2

    check-cast v2, Lcom/google/android/libraries/gsa/launcherclient/PrivateCallbacks;

    iget v4, p1, Landroid/os/Message;->arg1:I

    invoke-interface {v2, v4}, Lcom/google/android/libraries/gsa/launcherclient/PrivateCallbacks;->onExtraServiceStatus(I)V

    :cond_b5
    move v2, v3

    .line 882
    goto/16 :goto_7

    .line 849
    :pswitch_data_b8
    .packed-switch 0x2
        :pswitch_f
        :pswitch_5a
        :pswitch_8a
    .end packed-switch
.end method

.method public overlayScrollChanged(F)V
    .registers 5
    .param p1, "progress"    # F
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .prologue
    const/4 v2, 0x2

    .line 831
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->uIHandler:Landroid/os/Handler;

    invoke-virtual {v0, v2}, Landroid/os/Handler;->removeMessages(I)V

    .line 832
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->uIHandler:Landroid/os/Handler;

    invoke-static {p1}, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;

    move-result-object v1

    invoke-static {v0, v2, v1}, Landroid/os/Message;->obtain(Landroid/os/Handler;ILjava/lang/Object;)Landroid/os/Message;

    move-result-object v0

    invoke-virtual {v0}, Landroid/os/Message;->sendToTarget()V

    .line 833
    const/4 v0, 0x0

    cmpl-float v0, p1, v0

    if-lez v0, :cond_1c

    .line 834
    const/4 v0, 0x0

    invoke-direct {p0, v0}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->hideActivityNonUI(Z)V

    .line 836
    :cond_1c
    return-void
.end method

.method public overlayStatusChanged(I)V
    .registers 5
    .param p1, "status"    # I

    .prologue
    .line 840
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->uIHandler:Landroid/os/Handler;

    const/4 v1, 0x4

    const/4 v2, 0x0

    invoke-static {v0, v1, p1, v2}, Landroid/os/Message;->obtain(Landroid/os/Handler;III)Landroid/os/Message;

    move-result-object v0

    invoke-virtual {v0}, Landroid/os/Message;->sendToTarget()V

    .line 841
    return-void
.end method

.method public setClient(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)V
    .registers 5
    .param p1, "client"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
    .annotation build Landroid/annotation/TargetApi;
        value = 0x11
    .end annotation

    .prologue
    .line 815
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->client:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .line 816
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$600(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Landroid/app/Activity;

    move-result-object v1

    invoke-virtual {v1}, Landroid/app/Activity;->getWindowManager()Landroid/view/WindowManager;

    move-result-object v1

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowManager:Landroid/view/WindowManager;

    .line 817
    new-instance v0, Landroid/graphics/Point;

    invoke-direct {v0}, Landroid/graphics/Point;-><init>()V

    .line 818
    .local v0, "p":Landroid/graphics/Point;
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowManager:Landroid/view/WindowManager;

    invoke-interface {v1}, Landroid/view/WindowManager;->getDefaultDisplay()Landroid/view/Display;

    move-result-object v1

    invoke-virtual {v1, v0}, Landroid/view/Display;->getRealSize(Landroid/graphics/Point;)V

    .line 819
    iget v1, v0, Landroid/graphics/Point;->x:I

    iget v2, v0, Landroid/graphics/Point;->y:I

    invoke-static {v1, v2}, Ljava/lang/Math;->max(II)I

    move-result v1

    neg-int v1, v1

    iput v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->windowShift:I

    .line 820
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activity:Landroid/app/Activity;
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$600(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Landroid/app/Activity;

    move-result-object v1

    invoke-virtual {v1}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    move-result-object v1

    iput-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$OverlayCallbacks;->window:Landroid/view/Window;

    .line 821
    return-void
.end method
