.class public Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;
.super Ljava/lang/Object;
.source "LauncherClient.java"


# annotations
.annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
.end annotation

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x9
    name = "ClientOptions"
.end annotation


# instance fields
.field private final options:I


# direct methods
.method public constructor <init>(I)V
    .registers 2
    .param p1, "options"    # I

    .prologue
    .line 127
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 128
    iput p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->options:I

    .line 129
    return-void
.end method

.method public constructor <init>(ZZZ)V
    .registers 7
    .param p1, "enableOverlay"    # Z
    .param p2, "enableHotword"    # Z
    .param p3, "enablePrewarming"    # Z
    .annotation build Lcom/google/android/libraries/gsa/launcherclient/ThirdPartyApi;
    .end annotation

    .prologue
    const/4 v1, 0x0

    .line 118
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 119
    const/4 v0, 0x0

    .line 120
    .local v0, "options":I
    if-eqz p1, :cond_14

    const/4 v2, 0x1

    :goto_8
    or-int/2addr v0, v2

    .line 121
    if-eqz p2, :cond_16

    const/4 v2, 0x2

    :goto_c
    or-int/2addr v0, v2

    .line 122
    if-eqz p3, :cond_10

    const/4 v1, 0x4

    :cond_10
    or-int/2addr v0, v1

    .line 123
    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->options:I

    .line 124
    return-void

    :cond_14
    move v2, v1

    .line 120
    goto :goto_8

    :cond_16
    move v2, v1

    .line 121
    goto :goto_c
.end method

.method static synthetic access$400(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)I
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;

    .prologue
    .line 105
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->options:I

    return v0
.end method
