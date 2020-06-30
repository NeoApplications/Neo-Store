.class Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;
.super Ljava/lang/Object;
.source "EventLogArray.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0xa
    name = "EventEntry"
.end annotation


# instance fields
.field private duplicateCount:I

.field private event:Ljava/lang/String;

.field private extras:F

.field private time:J

.field private type:I


# direct methods
.method private constructor <init>()V
    .registers 1

    .prologue
    .line 103
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method synthetic constructor <init>(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$1;)V
    .registers 2
    .param p1, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$1;

    .prologue
    .line 103
    invoke-direct {p0}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;-><init>()V

    return-void
.end method

.method static synthetic access$000(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    .prologue
    .line 103
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->duplicateCount:I

    return v0
.end method

.method static synthetic access$008(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I
    .registers 3
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    .prologue
    .line 103
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->duplicateCount:I

    add-int/lit8 v1, v0, 0x1

    iput v1, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->duplicateCount:I

    return v0
.end method

.method static synthetic access$200(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)J
    .registers 3
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    .prologue
    .line 103
    iget-wide v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->time:J

    return-wide v0
.end method

.method static synthetic access$300(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)Ljava/lang/String;
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    .prologue
    .line 103
    iget-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->event:Ljava/lang/String;

    return-object v0
.end method

.method static synthetic access$400(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    .prologue
    .line 103
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->type:I

    return v0
.end method

.method static synthetic access$500(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)F
    .registers 2
    .param p0, "x0"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    .prologue
    .line 103
    iget v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->extras:F

    return v0
.end method


# virtual methods
.method public update(ILjava/lang/String;F)V
    .registers 6
    .param p1, "type"    # I
    .param p2, "event"    # Ljava/lang/String;
    .param p3, "extras"    # F

    .prologue
    .line 112
    iput p1, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->type:I

    .line 113
    iput-object p2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->event:Ljava/lang/String;

    .line 114
    iput p3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->extras:F

    .line 115
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v0

    iput-wide v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->time:J

    .line 116
    const/4 v0, 0x0

    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->duplicateCount:I

    .line 117
    return-void
.end method
