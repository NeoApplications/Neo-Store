.class public Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacksAdapter;
.super Ljava/lang/Object;
.source "LauncherClientCallbacksAdapter.java"

# interfaces
.implements Lcom/google/android/libraries/gsa/launcherclient/LauncherClientCallbacks;


# annotations
.annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
.end annotation


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 7
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onOverlayScrollChanged(F)V
    .registers 2
    .param p1, "progress"    # F
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 11
    return-void
.end method

.method public onServiceStateChanged(ZZ)V
    .registers 3
    .param p1, "overlayAttached"    # Z
    .param p2, "hotwordActive"    # Z
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    .line 15
    return-void
.end method
