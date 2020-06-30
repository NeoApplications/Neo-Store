.class Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;
.super Ljava/lang/Object;
.source "EventLogArray.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;
    }
.end annotation


# static fields
.field private static final TYPE_BOOL_FALSE:I = 0x4

.field private static final TYPE_BOOL_TRUE:I = 0x3

.field private static final TYPE_FLOAT:I = 0x1

.field private static final TYPE_INTEGER:I = 0x2

.field private static final TYPE_ONE_OFF:I


# instance fields
.field private final logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

.field private final name:Ljava/lang/String;

.field private nextIndex:I


# direct methods
.method public constructor <init>(Ljava/lang/String;I)V
    .registers 4
    .param p1, "name"    # Ljava/lang/String;
    .param p2, "size"    # I

    .prologue
    .line 24
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 25
    iput-object p1, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->name:Ljava/lang/String;

    .line 26
    new-array v0, p2, [Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    iput-object v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    .line 27
    const/4 v0, 0x0

    iput v0, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    .line 28
    return-void
.end method

.method private addLog(ILjava/lang/String;F)V
    .registers 10
    .param p1, "type"    # I
    .param p2, "event"    # Ljava/lang/String;
    .param p3, "extras"    # F

    .prologue
    .line 48
    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    iget-object v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v3, v3

    add-int/2addr v2, v3

    add-int/lit8 v2, v2, -0x1

    iget-object v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v3, v3

    rem-int v0, v2, v3

    .line 49
    .local v0, "last":I
    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    iget-object v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v3, v3

    add-int/2addr v2, v3

    add-int/lit8 v2, v2, -0x2

    iget-object v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v3, v3

    rem-int v1, v2, v3

    .line 50
    .local v1, "secondLast":I
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    aget-object v2, v2, v0

    invoke-static {v2, p1, p2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->isEntrySame(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;ILjava/lang/String;)Z

    move-result v2

    if-eqz v2, :cond_3d

    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    aget-object v2, v2, v1

    invoke-static {v2, p1, p2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->isEntrySame(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;ILjava/lang/String;)Z

    move-result v2

    if-eqz v2, :cond_3d

    .line 51
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    aget-object v2, v2, v0

    invoke-virtual {v2, p1, p2, p3}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->update(ILjava/lang/String;F)V

    .line 52
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    aget-object v2, v2, v1

    # operator++ for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->duplicateCount:I
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$008(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I

    .line 61
    :goto_3c
    return-void

    .line 56
    :cond_3d
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    iget v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    aget-object v2, v2, v3

    if-nez v2, :cond_51

    .line 57
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    iget v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    new-instance v4, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    const/4 v5, 0x0

    invoke-direct {v4, v5}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;-><init>(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$1;)V

    aput-object v4, v2, v3

    .line 59
    :cond_51
    iget-object v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    iget v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    aget-object v2, v2, v3

    invoke-virtual {v2, p1, p2, p3}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->update(ILjava/lang/String;F)V

    .line 60
    iget v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    add-int/lit8 v2, v2, 0x1

    iget-object v3, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v3, v3

    rem-int/2addr v2, v3

    iput v2, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    goto :goto_3c
.end method

.method private static isEntrySame(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;ILjava/lang/String;)Z
    .registers 4
    .param p0, "entry"    # Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;
    .param p1, "type"    # I
    .param p2, "event"    # Ljava/lang/String;

    .prologue
    .line 99
    if-eqz p0, :cond_14

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->type:I
    invoke-static {p0}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$400(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I

    move-result v0

    if-ne v0, p1, :cond_14

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->event:Ljava/lang/String;
    invoke-static {p0}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$300(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v0, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_14

    const/4 v0, 0x1

    :goto_13
    return v0

    :cond_14
    const/4 v0, 0x0

    goto :goto_13
.end method


# virtual methods
.method public addLog(Ljava/lang/String;)V
    .registers 4
    .param p1, "event"    # Ljava/lang/String;

    .prologue
    .line 31
    const/4 v0, 0x0

    const/4 v1, 0x0

    invoke-direct {p0, v0, p1, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(ILjava/lang/String;F)V

    .line 32
    return-void
.end method

.method public addLog(Ljava/lang/String;F)V
    .registers 4
    .param p1, "event"    # Ljava/lang/String;
    .param p2, "extras"    # F

    .prologue
    .line 39
    const/4 v0, 0x1

    invoke-direct {p0, v0, p1, p2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(ILjava/lang/String;F)V

    .line 40
    return-void
.end method

.method public addLog(Ljava/lang/String;I)V
    .registers 5
    .param p1, "event"    # Ljava/lang/String;
    .param p2, "extras"    # I

    .prologue
    .line 35
    const/4 v0, 0x2

    int-to-float v1, p2

    invoke-direct {p0, v0, p1, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(ILjava/lang/String;F)V

    .line 36
    return-void
.end method

.method public addLog(Ljava/lang/String;Z)V
    .registers 5
    .param p1, "event"    # Ljava/lang/String;
    .param p2, "extras"    # Z

    .prologue
    .line 43
    if-eqz p2, :cond_8

    const/4 v0, 0x3

    :goto_3
    const/4 v1, 0x0

    invoke-direct {p0, v0, p1, v1}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->addLog(ILjava/lang/String;F)V

    .line 44
    return-void

    .line 43
    :cond_8
    const/4 v0, 0x4

    goto :goto_3
.end method

.method public dump(Ljava/lang/String;Ljava/io/PrintWriter;)V
    .registers 11
    .param p1, "prefix"    # Ljava/lang/String;
    .param p2, "writer"    # Ljava/io/PrintWriter;

    .prologue
    .line 64
    iget-object v5, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->name:Ljava/lang/String;

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/String;->length()I

    move-result v6

    add-int/lit8 v6, v6, 0xf

    invoke-static {v5}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/String;->length()I

    move-result v7

    add-int/2addr v6, v7

    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7, v6}, Ljava/lang/StringBuilder;-><init>(I)V

    invoke-virtual {v7, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    const-string v6, " event history:"

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {p2, v5}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V

    .line 65
    new-instance v4, Ljava/text/SimpleDateFormat;

    const-string v5, "  HH:mm:ss.SSSZ  "

    sget-object v6, Ljava/util/Locale;->US:Ljava/util/Locale;

    invoke-direct {v4, v5, v6}, Ljava/text/SimpleDateFormat;-><init>(Ljava/lang/String;Ljava/util/Locale;)V

    .line 66
    .local v4, "sdf":Ljava/text/SimpleDateFormat;
    new-instance v0, Ljava/util/Date;

    invoke-direct {v0}, Ljava/util/Date;-><init>()V

    .line 68
    .local v0, "date":Ljava/util/Date;
    const/4 v1, 0x0

    .local v1, "i":I
    :goto_3e
    iget-object v5, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v5, v5

    if-ge v1, v5, :cond_c2

    .line 69
    iget-object v5, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    iget v6, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->nextIndex:I

    iget-object v7, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v7, v7

    add-int/2addr v6, v7

    sub-int/2addr v6, v1

    add-int/lit8 v6, v6, -0x1

    iget-object v7, p0, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray;->logs:[Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;

    array-length v7, v7

    rem-int/2addr v6, v7

    aget-object v2, v5, v6

    .line 70
    .local v2, "log":Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;
    if-nez v2, :cond_59

    .line 68
    :goto_56
    add-int/lit8 v1, v1, 0x1

    goto :goto_3e

    .line 73
    :cond_59
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->time:J
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$200(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)J

    move-result-wide v6

    invoke-virtual {v0, v6, v7}, Ljava/util/Date;->setTime(J)V

    .line 75
    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5, p1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v4, v0}, Ljava/text/SimpleDateFormat;->format(Ljava/util/Date;)Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->event:Ljava/lang/String;
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$300(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    .line 76
    .local v3, "msg":Ljava/lang/StringBuilder;
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->type:I
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$400(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I

    move-result v5

    packed-switch v5, :pswitch_data_c4

    .line 91
    :goto_7c
    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->duplicateCount:I
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$000(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I

    move-result v5

    if-lez v5, :cond_95

    .line 92
    const-string v5, " & "

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->duplicateCount:I
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$000(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)I

    move-result v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v5

    const-string v6, " similar events"

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 94
    :cond_95
    invoke-virtual {p2, v3}, Ljava/io/PrintWriter;->println(Ljava/lang/Object;)V

    goto :goto_56

    .line 78
    :pswitch_99
    const-string v5, ": false"

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto :goto_7c

    .line 81
    :pswitch_9f
    const-string v5, ": true"

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto :goto_7c

    .line 84
    :pswitch_a5
    const-string v5, ": "

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->extras:F
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$500(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)F

    move-result v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    goto :goto_7c

    .line 87
    :pswitch_b3
    const-string v5, ": "

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    # getter for: Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->extras:F
    invoke-static {v2}, Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;->access$500(Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;)F

    move-result v6

    float-to-int v6, v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    goto :goto_7c

    .line 96
    .end local v2    # "log":Lcom/google/android/libraries/gsa/launcherclient/EventLogArray$EventEntry;
    .end local v3    # "msg":Ljava/lang/StringBuilder;
    :cond_c2
    return-void

    .line 76
    nop

    :pswitch_data_c4
    .packed-switch 0x1
        :pswitch_a5
        :pswitch_b3
        :pswitch_9f
        :pswitch_99
    .end packed-switch
.end method
