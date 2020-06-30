.class public abstract Landroid/app/prediction/IPredictionCallback$Stub;
.super Landroid/os/Binder;
.source "IPredictionCallback.java"

# interfaces
.implements Landroid/app/prediction/IPredictionCallback;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Landroid/app/prediction/IPredictionCallback;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x409
    name = "Stub"
.end annotation

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Landroid/app/prediction/IPredictionCallback$Stub$Proxy;
    }
.end annotation


# static fields
.field private static final DESCRIPTOR:Ljava/lang/String; = "android.app.prediction.IPredictionCallback"

.field static final TRANSACTION_onResult:I = 0x1


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 27
    invoke-direct {p0}, Landroid/os/Binder;-><init>()V

    const-string v0, "android.app.prediction.IPredictionCallback"

    .line 28
    invoke-virtual {p0, p0, v0}, Landroid/app/prediction/IPredictionCallback$Stub;->attachInterface(Landroid/os/IInterface;Ljava/lang/String;)V

    return-void
.end method

.method public static asInterface(Landroid/os/IBinder;)Landroid/app/prediction/IPredictionCallback;
    .locals 2

    if-nez p0, :cond_0

    const/4 p0, 0x0

    return-object p0

    :cond_0
    const-string v0, "android.app.prediction.IPredictionCallback"

    .line 39
    invoke-interface {p0, v0}, Landroid/os/IBinder;->queryLocalInterface(Ljava/lang/String;)Landroid/os/IInterface;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 40
    instance-of v1, v0, Landroid/app/prediction/IPredictionCallback;

    if-eqz v1, :cond_1

    .line 41
    check-cast v0, Landroid/app/prediction/IPredictionCallback;

    return-object v0

    .line 43
    :cond_1
    new-instance v0, Landroid/app/prediction/IPredictionCallback$Stub$Proxy;

    invoke-direct {v0, p0}, Landroid/app/prediction/IPredictionCallback$Stub$Proxy;-><init>(Landroid/os/IBinder;)V

    return-object v0
.end method

.method public static getDefaultImpl()Landroid/app/prediction/IPredictionCallback;
    .locals 1

    .line 146
    sget-object v0, Landroid/app/prediction/IPredictionCallback$Stub$Proxy;->sDefaultImpl:Landroid/app/prediction/IPredictionCallback;

    return-object v0
.end method

.method public static getDefaultTransactionName(I)Ljava/lang/String;
    .locals 1

    const/4 v0, 0x1

    if-eq p0, v0, :cond_0

    const/4 p0, 0x0

    return-object p0

    :cond_0
    const-string p0, "onResult"

    return-object p0
.end method

.method public static setDefaultImpl(Landroid/app/prediction/IPredictionCallback;)Z
    .locals 1

    .line 139
    sget-object v0, Landroid/app/prediction/IPredictionCallback$Stub$Proxy;->sDefaultImpl:Landroid/app/prediction/IPredictionCallback;

    if-nez v0, :cond_0

    if-eqz p0, :cond_0

    .line 140
    sput-object p0, Landroid/app/prediction/IPredictionCallback$Stub$Proxy;->sDefaultImpl:Landroid/app/prediction/IPredictionCallback;

    const/4 p0, 0x1

    return p0

    :cond_0
    const/4 p0, 0x0

    return p0
.end method


# virtual methods
.method public asBinder()Landroid/os/IBinder;
    .locals 0

    return-object p0
.end method

.method public getTransactionName(I)Ljava/lang/String;
    .locals 0

    .line 67
    invoke-static {p1}, Landroid/app/prediction/IPredictionCallback$Stub;->getDefaultTransactionName(I)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public onTransact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    const/4 v0, 0x1

    const-string v1, "android.app.prediction.IPredictionCallback"

    if-eq p1, v0, :cond_1

    const v2, 0x5f4e5446

    if-eq p1, v2, :cond_0

    .line 94
    invoke-super {p0, p1, p2, p3, p4}, Landroid/os/Binder;->onTransact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result p0

    return p0

    .line 76
    :cond_0
    invoke-virtual {p3, v1}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    return v0

    .line 81
    :cond_1
    invoke-virtual {p2, v1}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 83
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result p1

    if-eqz p1, :cond_2

    .line 84
    sget-object p1, Landroid/content/pm/ParceledListSlice;->CREATOR:Landroid/os/Parcelable$ClassLoaderCreator;

    invoke-interface {p1, p2}, Landroid/os/Parcelable$ClassLoaderCreator;->createFromParcel(Landroid/os/Parcel;)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Landroid/content/pm/ParceledListSlice;

    goto :goto_0

    :cond_2
    const/4 p1, 0x0

    .line 89
    :goto_0
    invoke-virtual {p0, p1}, Landroid/app/prediction/IPredictionCallback$Stub;->onResult(Landroid/content/pm/ParceledListSlice;)V

    return v0
.end method
