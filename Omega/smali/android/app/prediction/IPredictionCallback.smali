.class public interface abstract Landroid/app/prediction/IPredictionCallback;
.super Ljava/lang/Object;
.source "IPredictionCallback.java"

# interfaces
.implements Landroid/os/IInterface;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Landroid/app/prediction/IPredictionCallback$Stub;,
        Landroid/app/prediction/IPredictionCallback$Default;
    }
.end annotation


# virtual methods
.method public abstract onResult(Landroid/content/pm/ParceledListSlice;)V
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation
.end method
