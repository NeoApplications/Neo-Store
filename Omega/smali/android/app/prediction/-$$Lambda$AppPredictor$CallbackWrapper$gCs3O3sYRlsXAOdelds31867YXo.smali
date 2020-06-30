.class public final synthetic Landroid/app/prediction/-$$Lambda$AppPredictor$CallbackWrapper$gCs3O3sYRlsXAOdelds31867YXo;
.super Ljava/lang/Object;
.source "lambda"

# interfaces
.implements Ljava/lang/Runnable;


# instance fields
.field private final synthetic f$0:Landroid/app/prediction/AppPredictor$CallbackWrapper;

.field private final synthetic f$1:Landroid/content/pm/ParceledListSlice;


# direct methods
.method public synthetic constructor <init>(Landroid/app/prediction/AppPredictor$CallbackWrapper;Landroid/content/pm/ParceledListSlice;)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Landroid/app/prediction/-$$Lambda$AppPredictor$CallbackWrapper$gCs3O3sYRlsXAOdelds31867YXo;->f$0:Landroid/app/prediction/AppPredictor$CallbackWrapper;

    iput-object p2, p0, Landroid/app/prediction/-$$Lambda$AppPredictor$CallbackWrapper$gCs3O3sYRlsXAOdelds31867YXo;->f$1:Landroid/content/pm/ParceledListSlice;

    return-void
.end method


# virtual methods
.method public final run()V
    .locals 1

    iget-object v0, p0, Landroid/app/prediction/-$$Lambda$AppPredictor$CallbackWrapper$gCs3O3sYRlsXAOdelds31867YXo;->f$0:Landroid/app/prediction/AppPredictor$CallbackWrapper;

    iget-object p0, p0, Landroid/app/prediction/-$$Lambda$AppPredictor$CallbackWrapper$gCs3O3sYRlsXAOdelds31867YXo;->f$1:Landroid/content/pm/ParceledListSlice;

    invoke-virtual {v0, p0}, Landroid/app/prediction/AppPredictor$CallbackWrapper;->lambda$onResult$0$AppPredictor$CallbackWrapper(Landroid/content/pm/ParceledListSlice;)V

    return-void
.end method
