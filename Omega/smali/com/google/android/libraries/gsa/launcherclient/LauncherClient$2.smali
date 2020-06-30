.class Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$2;
.super Ljava/lang/Object;
.source "LauncherClient.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->reconnect()V
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
    .line 441
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$2;->this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 3

    .prologue
    .line 444
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$2;->this$0:Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;

    const/4 v1, 0x0

    # invokes: Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->notifyStatusChanged(I)V
    invoke-static {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;->access$500(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;I)V

    .line 445
    return-void
.end method
