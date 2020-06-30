.class Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;
.super Landroid/content/BroadcastReceiver;
.source "LauncherClient.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;


# direct methods
.method constructor <init>(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)V
    .registers 2
    .param p1, "this$0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    .prologue
    .line 151
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;->this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 6
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 154
    invoke-virtual {p2}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object v0

    .line 156
    .local v0, "data":Landroid/net/Uri;
    sget v1, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v2, 0x13

    if-ge v1, v2, :cond_18

    if-eqz v0, :cond_3c

    const-string v1, "com.google.android.googlequicksearchbox"

    .line 157
    invoke-virtual {v0}, Landroid/net/Uri;->getSchemeSpecificPart()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_3c

    .line 159
    :cond_18
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;->this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityConnection:Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;
    invoke-static {v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$000(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;

    move-result-object v1

    invoke-virtual {v1}, Lcom/google/android/libraries/gsa/launcherclient/SimpleServiceConnection;->unbindSelf()V

    .line 160
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;->this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->appConnection:Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;
    invoke-static {v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$100(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;

    move-result-object v1

    invoke-virtual {v1}, Lcom/google/android/libraries/gsa/launcherclient/AppServiceConnection;->unbindSelf()V

    .line 161
    # invokes: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reloadServiceVersion(Landroid/content/Context;)V
    invoke-static {p1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$200(Landroid/content/Context;)V

    .line 162
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;->this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->activityState:I
    invoke-static {v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$300(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;)I

    move-result v1

    and-int/lit8 v1, v1, 0x2

    if-eqz v1, :cond_3c

    .line 163
    iget-object v1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$1;->this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-virtual {v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reconnect()V

    .line 166
    :cond_3c
    return-void
.end method
