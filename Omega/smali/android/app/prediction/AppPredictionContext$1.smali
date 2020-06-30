.class Landroid/app/prediction/AppPredictionContext$1;
.super Ljava/lang/Object;
.source "AppPredictionContext.java"

# interfaces
.implements Landroid/os/Parcelable$Creator;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Landroid/app/prediction/AppPredictionContext;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Object;",
        "Landroid/os/Parcelable$Creator<",
        "Landroid/app/prediction/AppPredictionContext;",
        ">;"
    }
.end annotation


# direct methods
.method constructor <init>()V
    .locals 0

    .line 117
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public createFromParcel(Landroid/os/Parcel;)Landroid/app/prediction/AppPredictionContext;
    .locals 1

    .line 119
    new-instance p0, Landroid/app/prediction/AppPredictionContext;

    const/4 v0, 0x0

    invoke-direct {p0, p1, v0}, Landroid/app/prediction/AppPredictionContext;-><init>(Landroid/os/Parcel;Landroid/app/prediction/AppPredictionContext$1;)V

    return-object p0
.end method

.method public bridge synthetic createFromParcel(Landroid/os/Parcel;)Ljava/lang/Object;
    .locals 0

    .line 117
    invoke-virtual {p0, p1}, Landroid/app/prediction/AppPredictionContext$1;->createFromParcel(Landroid/os/Parcel;)Landroid/app/prediction/AppPredictionContext;

    move-result-object p0

    return-object p0
.end method

.method public newArray(I)[Landroid/app/prediction/AppPredictionContext;
    .locals 0

    .line 123
    new-array p0, p1, [Landroid/app/prediction/AppPredictionContext;

    return-object p0
.end method

.method public bridge synthetic newArray(I)[Ljava/lang/Object;
    .locals 0

    .line 117
    invoke-virtual {p0, p1}, Landroid/app/prediction/AppPredictionContext$1;->newArray(I)[Landroid/app/prediction/AppPredictionContext;

    move-result-object p0

    return-object p0
.end method
