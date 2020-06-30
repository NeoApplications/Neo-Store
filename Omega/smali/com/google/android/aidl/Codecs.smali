.class public Lcom/google/android/aidl/Codecs;
.super Ljava/lang/Object;
.source "Codecs.java"


# static fields
.field private static final CLASS_LOADER:Ljava/lang/ClassLoader;

.field private static final PARCELABLE_NO_FLAGS:I


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 28
    const-class v0, Lcom/google/android/aidl/Codecs;

    invoke-virtual {v0}, Ljava/lang/Class;->getClassLoader()Ljava/lang/ClassLoader;

    move-result-object v0

    sput-object v0, Lcom/google/android/aidl/Codecs;->CLASS_LOADER:Ljava/lang/ClassLoader;

    return-void
.end method

.method private constructor <init>()V
    .registers 1

    .prologue
    .line 31
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static createBoolean(Landroid/os/Parcel;)Z
    .registers 2
    .param p0, "parcel"    # Landroid/os/Parcel;

    .prologue
    .line 34
    invoke-virtual {p0}, Landroid/os/Parcel;->readInt()I

    move-result v0

    if-eqz v0, :cond_8

    const/4 v0, 0x1

    :goto_7
    return v0

    :cond_8
    const/4 v0, 0x0

    goto :goto_7
.end method

.method public static createCharSequence(Landroid/os/Parcel;)Ljava/lang/CharSequence;
    .registers 2
    .param p0, "parcel"    # Landroid/os/Parcel;

    .prologue
    .line 42
    invoke-virtual {p0}, Landroid/os/Parcel;->readInt()I

    move-result v0

    if-nez v0, :cond_8

    .line 43
    const/4 v0, 0x0

    .line 45
    :goto_7
    return-object v0

    :cond_8
    sget-object v0, Landroid/text/TextUtils;->CHAR_SEQUENCE_CREATOR:Landroid/os/Parcelable$Creator;

    invoke-interface {v0, p0}, Landroid/os/Parcelable$Creator;->createFromParcel(Landroid/os/Parcel;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/CharSequence;

    goto :goto_7
.end method

.method public static createList(Landroid/os/Parcel;)Ljava/util/ArrayList;
    .registers 2
    .param p0, "parcel"    # Landroid/os/Parcel;

    .prologue
    .line 101
    sget-object v0, Lcom/google/android/aidl/Codecs;->CLASS_LOADER:Ljava/lang/ClassLoader;

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->readArrayList(Ljava/lang/ClassLoader;)Ljava/util/ArrayList;

    move-result-object v0

    return-object v0
.end method

.method public static createMap(Landroid/os/Parcel;)Ljava/util/HashMap;
    .registers 2
    .param p0, "parcel"    # Landroid/os/Parcel;

    .prologue
    .line 110
    sget-object v0, Lcom/google/android/aidl/Codecs;->CLASS_LOADER:Ljava/lang/ClassLoader;

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->readHashMap(Ljava/lang/ClassLoader;)Ljava/util/HashMap;

    move-result-object v0

    return-object v0
.end method

.method public static createParcelable(Landroid/os/Parcel;Landroid/os/Parcelable$Creator;)Landroid/os/Parcelable;
    .registers 3
    .param p0, "parcel"    # Landroid/os/Parcel;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<T::",
            "Landroid/os/Parcelable;",
            ">(",
            "Landroid/os/Parcel;",
            "Landroid/os/Parcelable$Creator",
            "<TT;>;)TT;"
        }
    .end annotation

    .prologue
    .line 67
    .local p1, "creator":Landroid/os/Parcelable$Creator;, "Landroid/os/Parcelable$Creator<TT;>;"
    invoke-virtual {p0}, Landroid/os/Parcel;->readInt()I

    move-result v0

    if-nez v0, :cond_8

    .line 68
    const/4 v0, 0x0

    .line 70
    :goto_7
    return-object v0

    :cond_8
    invoke-interface {p1, p0}, Landroid/os/Parcelable$Creator;->createFromParcel(Landroid/os/Parcel;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/os/Parcelable;

    goto :goto_7
.end method

.method public static readList(Landroid/os/Parcel;Ljava/util/List;)V
    .registers 3
    .param p0, "parcel"    # Landroid/os/Parcel;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/os/Parcel;",
            "Ljava/util/List",
            "<*>;)V"
        }
    .end annotation

    .prologue
    .line 105
    .local p1, "list":Ljava/util/List;, "Ljava/util/List<*>;"
    sget-object v0, Lcom/google/android/aidl/Codecs;->CLASS_LOADER:Ljava/lang/ClassLoader;

    invoke-virtual {p0, p1, v0}, Landroid/os/Parcel;->readList(Ljava/util/List;Ljava/lang/ClassLoader;)V

    .line 106
    return-void
.end method

.method public static readMap(Landroid/os/Parcel;Ljava/util/Map;)V
    .registers 3
    .param p0, "parcel"    # Landroid/os/Parcel;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/os/Parcel;",
            "Ljava/util/Map",
            "<**>;)V"
        }
    .end annotation

    .prologue
    .line 114
    .local p1, "map":Ljava/util/Map;, "Ljava/util/Map<**>;"
    sget-object v0, Lcom/google/android/aidl/Codecs;->CLASS_LOADER:Ljava/lang/ClassLoader;

    invoke-virtual {p0, p1, v0}, Landroid/os/Parcel;->readMap(Ljava/util/Map;Ljava/lang/ClassLoader;)V

    .line 115
    return-void
.end method

.method public static writeBoolean(Landroid/os/Parcel;Z)V
    .registers 3
    .param p0, "parcel"    # Landroid/os/Parcel;
    .param p1, "value"    # Z

    .prologue
    .line 38
    if-eqz p1, :cond_7

    const/4 v0, 0x1

    :goto_3
    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 39
    return-void

    .line 38
    :cond_7
    const/4 v0, 0x0

    goto :goto_3
.end method

.method public static writeCharSequence(Landroid/os/Parcel;Ljava/lang/CharSequence;)V
    .registers 4
    .param p0, "parcel"    # Landroid/os/Parcel;
    .param p1, "charSequence"    # Ljava/lang/CharSequence;

    .prologue
    const/4 v1, 0x0

    .line 49
    if-eqz p1, :cond_b

    .line 50
    const/4 v0, 0x1

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 51
    invoke-static {p1, p0, v1}, Landroid/text/TextUtils;->writeToParcel(Ljava/lang/CharSequence;Landroid/os/Parcel;I)V

    .line 55
    :goto_a
    return-void

    .line 53
    :cond_b
    invoke-virtual {p0, v1}, Landroid/os/Parcel;->writeInt(I)V

    goto :goto_a
.end method

.method public static writeCharSequenceAsReturnValue(Landroid/os/Parcel;Ljava/lang/CharSequence;)V
    .registers 3
    .param p0, "parcel"    # Landroid/os/Parcel;
    .param p1, "charSequence"    # Ljava/lang/CharSequence;

    .prologue
    const/4 v0, 0x1

    .line 58
    if-eqz p1, :cond_a

    .line 59
    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 60
    invoke-static {p1, p0, v0}, Landroid/text/TextUtils;->writeToParcel(Ljava/lang/CharSequence;Landroid/os/Parcel;I)V

    .line 64
    :goto_9
    return-void

    .line 62
    :cond_a
    const/4 v0, 0x0

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeInt(I)V

    goto :goto_9
.end method

.method public static writeParcelable(Landroid/os/Parcel;Landroid/os/Parcelable;)V
    .registers 4
    .param p0, "parcel"    # Landroid/os/Parcel;
    .param p1, "parcelable"    # Landroid/os/Parcelable;

    .prologue
    const/4 v1, 0x0

    .line 74
    if-nez p1, :cond_7

    .line 75
    invoke-virtual {p0, v1}, Landroid/os/Parcel;->writeInt(I)V

    .line 80
    :goto_6
    return-void

    .line 77
    :cond_7
    const/4 v0, 0x1

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 78
    invoke-interface {p1, p0, v1}, Landroid/os/Parcelable;->writeToParcel(Landroid/os/Parcel;I)V

    goto :goto_6
.end method

.method public static writeParcelableAsReturnValue(Landroid/os/Parcel;Landroid/os/Parcelable;)V
    .registers 3
    .param p0, "parcel"    # Landroid/os/Parcel;
    .param p1, "parcelable"    # Landroid/os/Parcelable;

    .prologue
    const/4 v0, 0x1

    .line 83
    if-nez p1, :cond_8

    .line 84
    const/4 v0, 0x0

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 89
    :goto_7
    return-void

    .line 86
    :cond_8
    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 87
    invoke-interface {p1, p0, v0}, Landroid/os/Parcelable;->writeToParcel(Landroid/os/Parcel;I)V

    goto :goto_7
.end method

.method public static writeStrongBinder(Landroid/os/Parcel;Landroid/os/IInterface;)V
    .registers 3
    .param p0, "parcel"    # Landroid/os/Parcel;
    .param p1, "iinterface"    # Landroid/os/IInterface;

    .prologue
    .line 92
    if-nez p1, :cond_7

    .line 93
    const/4 v0, 0x0

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeStrongBinder(Landroid/os/IBinder;)V

    .line 97
    :goto_6
    return-void

    .line 95
    :cond_7
    invoke-interface {p1}, Landroid/os/IInterface;->asBinder()Landroid/os/IBinder;

    move-result-object v0

    invoke-virtual {p0, v0}, Landroid/os/Parcel;->writeStrongBinder(Landroid/os/IBinder;)V

    goto :goto_6
.end method
