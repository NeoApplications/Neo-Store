.class public abstract Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;
.super Lcom/google/android/aidl/BaseStub;
.source "ILauncherOverlay.java"

# interfaces
.implements Lcom/google/android/libraries/launcherclient/ILauncherOverlay;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/google/android/libraries/launcherclient/ILauncherOverlay;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x409
    name = "Stub"
.end annotation

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub$Proxy;
    }
.end annotation


# static fields
.field private static final DESCRIPTOR:Ljava/lang/String; = "com.google.android.libraries.launcherclient.ILauncherOverlay"

.field static final TRANSACTION_closeOverlay:I = 0x6

.field static final TRANSACTION_endScroll:I = 0x3

.field static final TRANSACTION_getVoiceSearchLanguage:I = 0xb

.field static final TRANSACTION_hasOverlayContent:I = 0xd

.field static final TRANSACTION_isVoiceDetectionRunning:I = 0xc

.field static final TRANSACTION_onPause:I = 0x7

.field static final TRANSACTION_onResume:I = 0x8

.field static final TRANSACTION_onScroll:I = 0x2

.field static final TRANSACTION_openOverlay:I = 0x9

.field static final TRANSACTION_requestVoiceDetection:I = 0xa

.field static final TRANSACTION_setActivityState:I = 0x10

.field static final TRANSACTION_startScroll:I = 0x1

.field static final TRANSACTION_startSearch:I = 0x11

.field static final TRANSACTION_windowAttached:I = 0x4

.field static final TRANSACTION_windowAttached2:I = 0xe

.field static final TRANSACTION_windowDetached:I = 0x5


# direct methods
.method public constructor <init>()V
    .registers 2

    .prologue
    .line 158
    const-string v0, "com.google.android.libraries.launcherclient.ILauncherOverlay"

    invoke-direct {p0, v0}, Lcom/google/android/aidl/BaseStub;-><init>(Ljava/lang/String;)V

    .line 159
    return-void
.end method

.method public static asInterface(Landroid/os/IBinder;)Lcom/google/android/libraries/launcherclient/ILauncherOverlay;
    .registers 3
    .param p0, "obj"    # Landroid/os/IBinder;

    .prologue
    .line 162
    if-nez p0, :cond_4

    .line 163
    const/4 v0, 0x0

    .line 169
    :goto_3
    return-object v0

    .line 165
    :cond_4
    const-string v1, "com.google.android.libraries.launcherclient.ILauncherOverlay"

    invoke-interface {p0, v1}, Landroid/os/IBinder;->queryLocalInterface(Ljava/lang/String;)Landroid/os/IInterface;

    move-result-object v0

    .line 166
    .local v0, "iin":Landroid/os/IInterface;
    instance-of v1, v0, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    if-eqz v1, :cond_11

    .line 167
    check-cast v0, Lcom/google/android/libraries/launcherclient/ILauncherOverlay;

    goto :goto_3

    .line 169
    :cond_11
    new-instance v0, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub$Proxy;

    .end local v0    # "iin":Landroid/os/IInterface;
    invoke-direct {v0, p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub$Proxy;-><init>(Landroid/os/IBinder;)V

    goto :goto_3
.end method


# virtual methods
.method protected dispatchTransaction(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z
    .registers 17
    .param p1, "code"    # I
    .param p2, "data"    # Landroid/os/Parcel;
    .param p3, "reply"    # Landroid/os/Parcel;
    .param p4, "flags"    # I
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .prologue
    .line 175
    packed-switch p1, :pswitch_data_ac

    .line 262
    :pswitch_3
    const/4 v11, 0x0

    .line 265
    :goto_4
    return v11

    .line 177
    :pswitch_5
    invoke-virtual {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->startScroll()V

    .line 265
    :goto_8
    const/4 v11, 0x1

    goto :goto_4

    .line 181
    :pswitch_a
    invoke-virtual {p2}, Landroid/os/Parcel;->readFloat()F

    move-result v7

    .line 182
    .local v7, "progress":F
    invoke-virtual {p0, v7}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->onScroll(F)V

    goto :goto_8

    .line 186
    .end local v7    # "progress":F
    :pswitch_12
    invoke-virtual {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->endScroll()V

    goto :goto_8

    .line 190
    :pswitch_16
    sget-object v11, Landroid/view/WindowManager$LayoutParams;->CREATOR:Landroid/os/Parcelable$Creator;

    invoke-static {p2, v11}, Lcom/google/android/aidl/Codecs;->createParcelable(Landroid/os/Parcel;Landroid/os/Parcelable$Creator;)Landroid/os/Parcelable;

    move-result-object v0

    check-cast v0, Landroid/view/WindowManager$LayoutParams;

    .line 191
    .local v0, "attrs":Landroid/view/WindowManager$LayoutParams;
    invoke-virtual {p2}, Landroid/os/Parcel;->readStrongBinder()Landroid/os/IBinder;

    move-result-object v11

    invoke-static {v11}, Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback$Stub;->asInterface(Landroid/os/IBinder;)Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;

    move-result-object v1

    .line 192
    .local v1, "callbacks":Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result v5

    .line 193
    .local v5, "options":I
    invoke-virtual {p0, v0, v1, v5}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->windowAttached(Landroid/view/WindowManager$LayoutParams;Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;I)V

    goto :goto_8

    .line 197
    .end local v0    # "attrs":Landroid/view/WindowManager$LayoutParams;
    .end local v1    # "callbacks":Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;
    .end local v5    # "options":I
    :pswitch_2e
    sget-object v11, Landroid/os/Bundle;->CREATOR:Landroid/os/Parcelable$Creator;

    invoke-static {p2, v11}, Lcom/google/android/aidl/Codecs;->createParcelable(Landroid/os/Parcel;Landroid/os/Parcelable$Creator;)Landroid/os/Parcelable;

    move-result-object v6

    check-cast v6, Landroid/os/Bundle;

    .line 198
    .local v6, "params":Landroid/os/Bundle;
    invoke-virtual {p2}, Landroid/os/Parcel;->readStrongBinder()Landroid/os/IBinder;

    move-result-object v11

    invoke-static {v11}, Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback$Stub;->asInterface(Landroid/os/IBinder;)Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;

    move-result-object v1

    .line 199
    .restart local v1    # "callbacks":Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;
    invoke-virtual {p0, v6, v1}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->windowAttached2(Landroid/os/Bundle;Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;)V

    goto :goto_8

    .line 203
    .end local v1    # "callbacks":Lcom/google/android/libraries/launcherclient/ILauncherOverlayCallback;
    .end local v6    # "params":Landroid/os/Bundle;
    :pswitch_42
    invoke-static {p2}, Lcom/google/android/aidl/Codecs;->createBoolean(Landroid/os/Parcel;)Z

    move-result v4

    .line 204
    .local v4, "isChangingConfigurations":Z
    invoke-virtual {p0, v4}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->windowDetached(Z)V

    goto :goto_8

    .line 208
    .end local v4    # "isChangingConfigurations":Z
    :pswitch_4a
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result v5

    .line 209
    .restart local v5    # "options":I
    invoke-virtual {p0, v5}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->closeOverlay(I)V

    goto :goto_8

    .line 213
    .end local v5    # "options":I
    :pswitch_52
    invoke-virtual {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->onPause()V

    goto :goto_8

    .line 217
    :pswitch_56
    invoke-virtual {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->onResume()V

    goto :goto_8

    .line 221
    :pswitch_5a
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result v10

    .line 222
    .local v10, "stateFlags":I
    invoke-virtual {p0, v10}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->setActivityState(I)V

    goto :goto_8

    .line 226
    .end local v10    # "stateFlags":I
    :pswitch_62
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result v5

    .line 227
    .restart local v5    # "options":I
    invoke-virtual {p0, v5}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->openOverlay(I)V

    goto :goto_8

    .line 231
    .end local v5    # "options":I
    :pswitch_6a
    invoke-static {p2}, Lcom/google/android/aidl/Codecs;->createBoolean(Landroid/os/Parcel;)Z

    move-result v9

    .line 232
    .local v9, "start":Z
    invoke-virtual {p0, v9}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->requestVoiceDetection(Z)V

    goto :goto_8

    .line 236
    .end local v9    # "start":Z
    :pswitch_72
    invoke-virtual {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->getVoiceSearchLanguage()Ljava/lang/String;

    move-result-object v8

    .line 237
    .local v8, "retval":Ljava/lang/String;
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    .line 238
    invoke-virtual {p3, v8}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    goto :goto_8

    .line 242
    .end local v8    # "retval":Ljava/lang/String;
    :pswitch_7d
    invoke-virtual {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->isVoiceDetectionRunning()Z

    move-result v8

    .line 243
    .local v8, "retval":Z
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    .line 244
    invoke-static {p3, v8}, Lcom/google/android/aidl/Codecs;->writeBoolean(Landroid/os/Parcel;Z)V

    goto :goto_8

    .line 248
    .end local v8    # "retval":Z
    :pswitch_88
    invoke-virtual {p0}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->hasOverlayContent()Z

    move-result v8

    .line 249
    .restart local v8    # "retval":Z
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    .line 250
    invoke-static {p3, v8}, Lcom/google/android/aidl/Codecs;->writeBoolean(Landroid/os/Parcel;Z)V

    goto/16 :goto_8

    .line 254
    .end local v8    # "retval":Z
    :pswitch_94
    invoke-virtual {p2}, Landroid/os/Parcel;->createByteArray()[B

    move-result-object v2

    .line 255
    .local v2, "config":[B
    sget-object v11, Landroid/os/Bundle;->CREATOR:Landroid/os/Parcelable$Creator;

    invoke-static {p2, v11}, Lcom/google/android/aidl/Codecs;->createParcelable(Landroid/os/Parcel;Landroid/os/Parcelable$Creator;)Landroid/os/Parcelable;

    move-result-object v3

    check-cast v3, Landroid/os/Bundle;

    .line 256
    .local v3, "extras":Landroid/os/Bundle;
    invoke-virtual {p0, v2, v3}, Lcom/google/android/libraries/launcherclient/ILauncherOverlay$Stub;->startSearch([BLandroid/os/Bundle;)Z

    move-result v8

    .line 257
    .restart local v8    # "retval":Z
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    .line 258
    invoke-static {p3, v8}, Lcom/google/android/aidl/Codecs;->writeBoolean(Landroid/os/Parcel;Z)V

    goto/16 :goto_8

    .line 175
    :pswitch_data_ac
    .packed-switch 0x1
        :pswitch_5
        :pswitch_a
        :pswitch_12
        :pswitch_16
        :pswitch_42
        :pswitch_4a
        :pswitch_52
        :pswitch_56
        :pswitch_62
        :pswitch_6a
        :pswitch_72
        :pswitch_7d
        :pswitch_88
        :pswitch_2e
        :pswitch_3
        :pswitch_5a
        :pswitch_94
    .end packed-switch
.end method
