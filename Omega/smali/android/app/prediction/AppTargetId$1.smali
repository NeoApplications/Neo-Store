.class Landroid/app/prediction/AppTargetId$1;
.super Ljava/lang/Object;
.source "AppTargetId.java"

# interfaces
.implements Landroid/os/Parcelable$Creator;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Landroid/app/prediction/AppTargetId;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Object;",
        "Landroid/os/Parcelable$Creator<",
        "Landroid/app/prediction/AppTargetId;",
        ">;"
    }
.end annotation


# direct methods
.method constructor <init>()V
    .locals 0

    .line 85
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public createFromParcel(Landroid/os/Parcel;)Landroid/app/prediction/AppTargetId;
    .locals 1

    .line 87
    new-instance p0, Landroid/app/prediction/AppTargetId;

    const/4 v0, 0x0

    invoke-direct {p0, p1, v0}, Landroid/app/prediction/AppTargetId;-><init>(Landroid/os/Parcel;Landroid/app/prediction/AppTargetId$1;)V

    return-object p0
.end method

.method public bridge synthetic createFromParcel(Landroid/os/Parcel;)Ljava/lang/Object;
    .locals 0

    .line 85
    invoke-virtual {p0, p1}, Landroid/app/prediction/AppTargetId$1;->createFromParcel(Landroid/os/Parcel;)Landroid/app/prediction/AppTargetId;

    move-result-object p0

    return-object p0
.end method

.method public newArray(I)[Landroid/app/prediction/AppTargetId;
    .locals 0

    .line 91
    new-array p0, p1, [Landroid/app/prediction/AppTargetId;

    return-object p0
.end method

.method public bridge synthetic newArray(I)[Ljava/lang/Object;
    .locals 0

    .line 85
    invoke-virtual {p0, p1}, Landroid/app/prediction/AppTargetId$1;->newArray(I)[Landroid/app/prediction/AppTargetId;

    move-result-object p0

    return-object p0
.end method
