package com.google.android.apps.nexuslauncher.smartspace;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ResourceUtils;
import com.android.launcher3.icons.GraphicsUtils;
import com.android.launcher3.icons.ShadowGenerator;
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.CardWrapper;
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.b;
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.c;
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.e;
import com.google.android.apps.nexuslauncher.utils.ColorManipulation;
import com.google.protobuf.ByteString;
import com.saggitt.omega.smartspace.FeedBridge;

public class SmartspaceCard {
    private final b dI;
    private final long dJ;
    private final int dK;
    private final boolean dL;
    private final boolean dM;
    private final long dN;
    private final Context mContext;
    private Bitmap mIcon;
    private final Intent mIntent;

    public SmartspaceCard(final Context context, final b di, final Intent mIntent, final boolean dm,
                          final Bitmap mIcon, final boolean dl, final long dn, final long dj, final int dk) {
        this.mContext = context.getApplicationContext();
        this.dI = di;
        this.dM = dm;
        this.mIntent = mIntent;
        this.mIcon = mIcon;
        this.dN = dn;
        this.dJ = dj;
        this.dK = dk;
        this.dL = dl;
    }

    static SmartspaceCard cD(Context context, CardWrapper iVar, boolean z) {
        if (iVar != null) {
            try {
                Intent parseUri = TextUtils.isEmpty(iVar.getDe().getCG().getCZ()) ?
                        null :
                        Intent.parseUri(iVar.getDe().getCG().getCZ(), 0);

                Bitmap bitmap = iVar.getDd() == null ?
                        null :
                        BitmapFactory.decodeByteArray(iVar.getDd().toByteArray(), 0, iVar.getDd().size(), null);

                if (bitmap != null) {
                    ShadowGenerator shadowGenerator = new ShadowGenerator(
                            ResourceUtils.pxFromDp(48, context.getResources().getDisplayMetrics()));
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
                    shadowGenerator.recreateIcon(bitmap, new Canvas(newBitmap));
                    bitmap = newBitmap;
                }

                return new SmartspaceCard(context, iVar.getDe(), parseUri, z, bitmap, iVar.getDc(), iVar.getDf(), iVar.getDh(), iVar.getDg());
            } catch (Throwable e) {
                Log.e("SmartspaceCard", "from proto", e);
            }
        }

        return null;
    }

    private String cE(final e eVar) {
        final Resources res = mContext.getResources();
        int minutesToEvent = cJ(eVar);
        if (minutesToEvent >= 60) {
            int hours = minutesToEvent / 60;
            int minutes = minutesToEvent % 60;
            String hoursString = res.getQuantityString(R.plurals.smartspace_hours, hours, hours);
            if (minutes <= 0) {
                return hoursString;
            }
            String minutesString = res.getQuantityString(R.plurals.smartspace_minutes, minutes, minutes);
            return res.getString(R.string.smartspace_hours_mins, hoursString, minutesString);
        }
        return res.getQuantityString(R.plurals.smartspace_minutes, minutesToEvent, minutesToEvent);
    }

    private com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.d cG(final boolean b) {
        final c ch = this.cH();
        if (ch != null) {
            com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.d d;
            if (b) {
                d = ch.getCL();
            } else {
                d = ch.getCM();
            }
            return d;
        }
        return null;
    }

    private c cH() {
        final long currentTimeMillis = System.currentTimeMillis();
        final long cd = this.dI.getCD();
        final long n = this.dI.getCD() + this.dI.getCE();
        if (currentTimeMillis < cd && this.dI.getCB() != null) {
            return this.dI.getCB();
        }
        if (currentTimeMillis > n && this.dI.getCH() != null) {
            return this.dI.getCH();
        }
        if (this.dI.getCC() != null) {
            return this.dI.getCC();
        }
        return null;
    }

    private int cJ(final e e) {
        return (int) Math.ceil(this.cI(e) / 60000.0);
    }

    private String[] cK(final e[] array, final String s) {
        int i;
        String[] array2;
        String cr;
        for (i = 0, array2 = new String[array.length]; i < array2.length; ++i) {
            switch (array[i].getCQ()) {
                default: {
                    array2[i] = "";
                    break;
                }
                case 3: {
                    if (s != null && array[i].getCS() != 0) {
                        array2[i] = s;
                        break;
                    }
                    if (array[i].getCR() != null) {
                        cr = array[i].getCR();
                    } else {
                        cr = "";
                    }
                    array2[i] = cr;
                    break;
                }
                case 1:
                case 2: {
                    array2[i] = this.cE(array[i]);
                    break;
                }
            }
        }
        return array2;
    }

    private boolean cL(final com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.d d) {
        boolean b = false;
        if (d != null && d.getCN() != null && d.getCOList() != null && d.getCOList().size() > 0) {
            b = true;
        }
        return b;
    }

    private String cN(final boolean b) {
        return this.cO(b, null);
    }

    private String cO(final boolean b, final String s) {
        final com.google.android.apps.nexuslauncher.smartspace.SmartspaceProto.d cg = this.cG(b);
        if (cg == null || cg.getCN() == null) {
            return "";
        }
        String cn = cg.getCN();
        if (this.cL(cg)) {
            return String.format(cn, (Object[]) this.cK(cg.getCOList().toArray(new e[0]), s));
        }
        if (cn == null) {
            cn = "";
        }
        return cn;
    }

    private static Bitmap cP(final Bitmap bitmap, final int n) {
        final Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(n, PorterDuff.Mode.SRC_IN));
        final Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(bitmap2).drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmap2;
    }

    static CardWrapper cQ(final Context context, final NewCardInfo a) {
        if (a == null) {
            return null;
        }
        CardWrapper.Builder builder = CardWrapper.newBuilder();
        final Bitmap ci = a.getBitmap(context);
        Bitmap cp;
        if (ci != null && builder.getDc()) {
            if (a.dj) {
                cp = cP(ci, -1);
            } else {
                cp = ci;
            }
        } else {
            cp = ci;
        }
        byte[] flattenBitmap;
        if (cp != null) {
            flattenBitmap = GraphicsUtils.flattenBitmap(cp);
        } else {
            flattenBitmap = new byte[0];
        }
        builder.setDd(ByteString.copyFrom(flattenBitmap));
        builder.setDc(cp != null && new ColorManipulation().dB(cp));
        builder.setDe(a.di);
        builder.setDf(a.dl);
        if (a.dk != null) {
            builder.setDg(a.dk.versionCode);
            builder.setDh(a.dk.lastUpdateTime);
        }
        return builder.build();
    }

    public String cA(final boolean b) {
        return this.cO(b, "");
    }

    public String cB(final boolean b) {
        int i = 0;
        final e[] co = this.cG(b).getCOList().toArray(new e[0]);
        if (co != null) {
            while (i < co.length) {
                if (co[i].getCS() != 0) {
                    return co[i].getCR();
                }
                ++i;
            }
        }
        return "";
    }

    public String cC(final String s) {
        return this.cO(true, s);
    }

    public long cF() {
        return this.dI.getCF().getDa();
    }

    long cI(final e e) {
        long cd;
        if (e.getCQ() == 2) {
            cd = this.dI.getCD() + this.dI.getCE();
        } else {
            cd = this.dI.getCE();
        }
        return Math.abs(System.currentTimeMillis() - cd);
    }

    public boolean cM() {
        return System.currentTimeMillis() > this.cF();
    }

    void click(View view) {
        if (this.dI.getCG() == null) {
            Log.e("SmartspaceCard", "no tap action available: " + this);
            return;
        }
        Intent intent = new Intent(this.getIntent());
        Launcher launcher = Launcher.getLauncher(view.getContext());
        switch (this.dI.getCG().getCY()) {
            default: {
                Log.w("SmartspaceCard", "unrecognized tap action: " + this);
                break;
            }
            case 1: {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setSourceBounds(launcher.getViewBounds(view));
                intent.setPackage(FeedBridge.Companion.getInstance(mContext).resolveSmartspace());
                view.getContext().sendBroadcast(intent);
                break;
            }
            case 2: {
                launcher.startActivitySafely(view, intent, null);
                break;
            }
        }
    }

    public boolean cv() {
        final c ch = this.cH();
        return ch != null && (this.cL(ch.getCL()) || this.cL(ch.getCL()));
    }

    public long cw() {
        final c ch = this.cH();
        if (ch != null && this.cL(ch.getCL())) {
            final e[] co = ch.getCL().getCOList().toArray(new e[0]);
            for (int i = 0; i < co.length; ++i) {
                final e e = co[i];
                if (e.getCQ() == 1 || e.getCQ() == 2) {
                    return this.cI(e);
                }
            }
        }
        return 0L;
    }

    public TextUtils.TruncateAt cx(final boolean b) {
        final c ch = this.cH();
        if (ch != null) {
            int n = 0;
            if (b && ch.getCL() != null) {
                n = ch.getCL().getCP();
            } else if (!b && ch.getCM() != null) {
                n = ch.getCM().getCP();
            }
            switch (n) {
                case 1: {
                    return TextUtils.TruncateAt.START;
                }
                case 2: {
                    return TextUtils.TruncateAt.MIDDLE;
                }
            }
        }
        return TextUtils.TruncateAt.END;
    }

    public String cy() {
        return this.cN(false);
    }

    public boolean cz() {
        return this.dL;
    }

    public Bitmap getIcon() {
        return this.mIcon;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public String getTitle() {
        return this.cN(true);
    }

    public String toString() {
        return "title:" + this.getTitle() + " expires:" + this.cF() + " published:" + this.dN + " gsaVersion:" + this.dK + " gsaUpdateTime: " + this.dJ;
    }
}
