.class Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$1;
.super Ljava/lang/Object;
.source "AbsServiceStatusChecker.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->checkServiceStatus(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;Landroid/content/Intent;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

.field final synthetic val$statusCallback:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;


# direct methods
.method constructor <init>(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;)V
    .registers 3
    .param p1, "this$0"    # Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    .prologue
    .line 50
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$1;->this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    iput-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$1;->val$statusCallback:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 3

    .prologue
    .line 53
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$1;->this$0:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;

    # invokes: Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->assertMainThread()V
    invoke-static {v0}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;->access$000(Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker;)V

    .line 57
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$1;->val$statusCallback:Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;

    const/4 v1, 0x0

    invoke-interface {v0, v1}, Lcom/google/android/libraries/gsa/launcherclient/AbsServiceStatusChecker$StatusCallback;->isRunning(Z)V

    .line 58
    return-void
.end method
