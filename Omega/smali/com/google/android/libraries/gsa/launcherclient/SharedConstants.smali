.class public Lcom/google/android/libraries/gsa/launcherclient/SharedConstants;
.super Ljava/lang/Object;
.source "SharedConstants.java"


# static fields
.field public static final ACTION_OVERLAY_SERVICE:Ljava/lang/String; = "com.android.launcher3.WINDOW_OVERLAY"

.field public static final ACTIVITY_STATE_RESUMED:I = 0x2

.field public static final ACTIVITY_STATE_STARTED:I = 0x1

.field public static final CLIENT_VERSION:Ljava/lang/String; = "cv"

.field public static final EXTRA_BACKGROUND_COLOR_HINT:Ljava/lang/String; = "background_color_hint"

.field public static final EXTRA_BACKGROUND_IS_DARK:Ljava/lang/String; = "is_background_dark"

.field public static final EXTRA_BACKGROUND_SECONDARY_COLOR_HINT:Ljava/lang/String; = "background_secondary_color_hint"

.field public static final EXTRA_CLIENT_OPTIONS:Ljava/lang/String; = "client_options"

.field public static final EXTRA_CLIP_DATA:Ljava/lang/String; = "clip_data"

.field public static final EXTRA_CONFIGURATION:Ljava/lang/String; = "configuration"

.field public static final EXTRA_LAYOUT_PARAMS:Ljava/lang/String; = "layout_params"

.field public static final EXTRA_SYSTEM_UI_VISIBILITY:Ljava/lang/String; = "system_ui_visibility"

.field public static final GSA_PACKAGE:Ljava/lang/String; = "com.google.android.googlequicksearchbox"

.field public static final MIN_SERVICE_VERSION_FOR_SET_ACTIVITY_STATE:I = 0x4

.field public static final MIN_SERVICE_VERSION_FOR_START_SEARCH:I = 0x6

.field public static final MIN_SERVICE_VERSION_FOR_TOKEN_REAPPLY:I = 0x7

.field public static final OPTIONS_FLAG_ALL:I = 0xf

.field public static final OPTIONS_FLAG_APPS_SEARCH:I = 0x8

.field public static final OPTIONS_FLAG_DEFAULT:I = 0x7

.field public static final OPTIONS_FLAG_HOTWORD:I = 0x2

.field public static final OPTIONS_FLAG_OVERLAY:I = 0x1

.field public static final OPTIONS_FLAG_PREWARM:I = 0x4

.field public static final OVERLAY_OPTION_ANIMATE_DURATION_BIT_SHIFT:I = 0x2

.field public static final OVERLAY_OPTION_ANIMATE_DURATION_MASK:I = 0x7ff

.field public static final OVERLAY_OPTION_FLAG_ANIMATE:I = 0x1

.field public static final OVERLAY_OPTION_FLAG_IMMEDIATE:I = 0x0

.field public static final OVERLAY_OPTION_FLAG_LAYERED:I = 0x2

.field public static final OVERLAY_OPTION_FLAG_PUBLIC_ALL:I = 0x1ffd

.field public static final SERVER_VERSION:Ljava/lang/String; = "v"

.field public static final SERVICE_STATUS_ALL_FEATURES_OFF:I = 0x0

.field public static final SERVICE_STATUS_HOTWORD_ACTIVE:I = 0x2

.field public static final SERVICE_STATUS_OVERLAY_ATTACHED:I = 0x1

.field public static final SERVICE_STATUS_PIXEL_2017_SEARCH_UI:I = 0x8

.field public static final SERVICE_STATUS_PIXEL_2017_SEARCH_UI_LANDSCAPE:I = 0x10

.field public static final SERVICE_STATUS_PREWARM_ACTIVE:I = 0x4


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 8
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
