package com.google.systemui.smartspace;

import static com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message.FormattedText.FormatParam.FormatParamArgs.SOMETHING1;
import static com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message.FormattedText.FormatParam.FormatParamArgs.SOMETHING2;

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
import com.google.android.apps.nexuslauncher.utils.ColorManipulation;
import com.google.android.systemui.smartspace.SmartspaceProto;
import com.google.android.systemui.smartspace.SmartspaceProto.CardWrapper;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message.FormattedText;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message.FormattedText.FormatParam;
import com.google.protobuf.ByteString;
import com.saggitt.omega.smartspace.FeedBridge;

public class SmartSpaceCardView {
    private final SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard dI;
    private final long dJ;
    private final int dK;
    private final boolean dL;
    private final boolean dM;
    private final long dN;
    private final Context mContext;
    private Bitmap mIcon;
    private final Intent mIntent;

    public SmartSpaceCardView(final Context context, final SmartSpaceCard di, final Intent mIntent, final boolean dm,
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

    static SmartSpaceCardView cD(Context context, CardWrapper iVar, boolean z) {
        if (iVar != null) {
            try {
                Intent parseUri = TextUtils.isEmpty(iVar.getCard().getTapAction().getIntent()) ?
                        null :
                        Intent.parseUri(iVar.getCard().getTapAction().getIntent(), 0);

                Bitmap bitmap = iVar.getIcon() == null ?
                        null :
                        BitmapFactory.decodeByteArray(iVar.getIcon().toByteArray(), 0, iVar.getIcon().size(), null);

                if (bitmap != null) {
                    ShadowGenerator shadowGenerator = new ShadowGenerator(
                            ResourceUtils.pxFromDp(48, context.getResources().getDisplayMetrics()));
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
                    shadowGenerator.recreateIcon(bitmap, new Canvas(newBitmap));
                    bitmap = newBitmap;
                }

                return new SmartSpaceCardView(context, iVar.getCard(), parseUri, z, bitmap, iVar.getIsIconGrayscale(),
                        iVar.getPublishTime(), iVar.getGsaUpdateTime(), iVar.getGsaVersionCode());
            } catch (Throwable e) {
                Log.e("SmartspaceCardView", "from proto", e);
            }
        }

        return null;
    }

    private String cE(final FormatParam eVar) {
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

    private FormattedText cG(final boolean b) {
        final SmartSpaceCard.Message ch = this.cH();
        if (ch != null) {
            FormattedText d;
            if (b) {
                d = ch.getTitle();
            } else {
                d = ch.getSubtitle();
            }
            return d;
        }
        return null;
    }

    private SmartSpaceCard.Message cH() {
        final long currentTimeMillis = System.currentTimeMillis();
        final long cd = this.dI.getEventTimeMillis();
        final long n = this.dI.getEventTimeMillis() + this.dI.getEventDurationMillis();
        if (currentTimeMillis < cd && this.dI.getPreEvent() != null) {
            return this.dI.getPreEvent();
        }
        if (currentTimeMillis > n && this.dI.getPostEvent() != null) {
            return this.dI.getPostEvent();
        }
        if (this.dI.getDuringEvent() != null) {
            return this.dI.getDuringEvent();
        }
        return null;
    }

    private int cJ(final FormatParam e) {
        return (int) Math.ceil(this.cI(e) / 60000.0);
    }

    private String[] cK(final FormatParam[] array, final String s) {
        int i;
        String[] array2;
        String cr;
        for (i = 0, array2 = new String[array.length]; i < array2.length; ++i) {
            switch (array[i].getFormatParamArgs()) {

                case SOMETHING1:
                case SOMETHING2: {
                    array2[i] = this.cE(array[i]);
                    break;
                }

                case SOMETHING3: {
                    if (s != null && array[i].getTruncateLocation() != 0) {
                        array2[i] = s;
                        break;
                    }
                    if (array[i].getText() != null) {
                        cr = array[i].getText();
                    } else {
                        cr = "";
                    }
                    array2[i] = cr;
                    break;
                }

                default: {
                    array2[i] = "";
                    break;
                }
            }
        }
        return array2;
    }

    private boolean cL(final FormattedText d) {
        boolean b = false;
        if (d != null && d.getText() != null && d.getFormatParamList() != null && d.getFormatParamList().size() > 0) {
            b = true;
        }
        return b;
    }

    private String cN(final boolean b) {
        return this.cO(b, null);
    }

    private String cO(final boolean b, final String s) {
        final FormattedText cg = this.cG(b);
        if (cg == null || cg.getText() == null) {
            return "";
        }
        String cn = cg.getText();
        if (this.cL(cg)) {
            return String.format(cn, (Object[]) this.cK(cg.getFormatParamList().toArray(new FormatParam[0]), s));
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
        final Bitmap ci = a.retrieveIcon(context);
        Bitmap cp;
        if (ci != null && builder.getIsIconGrayscale()) {
            if (a.mIsPrimary) {
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
        builder.setIcon(ByteString.copyFrom(flattenBitmap));
        builder.setIsIconGrayscale(cp != null && new ColorManipulation().dB(cp));
        builder.setCard(a.mCard);
        builder.setPublishTime(a.mPublishTime);
        if (a.mPackageInfo != null) {
            builder.setGsaVersionCode(a.mPackageInfo.versionCode);
            builder.setGsaUpdateTime(a.mPackageInfo.lastUpdateTime);
        }
        return builder.build();
    }

    public String cA(final boolean b) {
        return this.cO(b, "");
    }

    public String cB(final boolean b) {
        int i = 0;
        final FormatParam[] co = this.cG(b).getFormatParamList().toArray(new FormatParam[0]);
        if (co != null) {
            while (i < co.length) {
                if (co[i].getTruncateLocation() != 0) {
                    return co[i].getText();
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
        return this.dI.getExpiryCriteria().getExpirationTimeMillis();
    }

    long cI(final FormatParam e) {
        long cd;
        if (e.getFormatParamArgs() == SOMETHING2) {
            cd = this.dI.getEventTimeMillis() + this.dI.getEventDurationMillis();
        } else {
            cd = this.dI.getEventDurationMillis();
        }
        return Math.abs(System.currentTimeMillis() - cd);
    }

    public boolean cM() {
        return System.currentTimeMillis() > this.cF();
    }

    void click(View view) {
        if (this.dI.getTapAction() == null) {
            Log.e("SmartspaceCardView", "no tap action available: " + this);
            return;
        }
        Intent intent = new Intent(this.getIntent());
        Launcher launcher = Launcher.getLauncher(view.getContext());
        switch (this.dI.getTapAction().getActionType()) {
            case ACTION1: {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setSourceBounds(launcher.getViewBounds(view));
                intent.setPackage(FeedBridge.Companion.getInstance(mContext).resolveSmartspace());
                view.getContext().sendBroadcast(intent);
                break;
            }
            case ACTION2: {
                launcher.startActivitySafely(view, intent, null);
                break;
            }
            default: {
                Log.w("SmartspaceCardView", "unrecognized tap action: " + this);
                break;
            }
        }
    }

    public boolean cv() {
        final SmartSpaceCard.Message ch = this.cH();
        return ch != null && (this.cL(ch.getTitle()) || this.cL(ch.getTitle()));
    }

    public long cw() {
        final SmartSpaceCard.Message ch = this.cH();
        if (ch != null && this.cL(ch.getTitle())) {
            final FormatParam[] co = ch.getTitle().getFormatParamList().toArray(new FormatParam[0]);
            for (int i = 0; i < co.length; ++i) {
                final FormatParam e = co[i];
                if (e.getFormatParamArgs() == SOMETHING1 || e.getFormatParamArgs() == SOMETHING2) {
                    return this.cI(e);
                }
            }
        }
        return 0L;
    }

    public TextUtils.TruncateAt cx(final boolean b) {
        final SmartSpaceCard.Message ch = this.cH();
        if (ch != null) {
            int n = 0;
            if (b && ch.getTitle() != null) {
                n = ch.getTitle().getTruncateLocation();
            } else if (!b && ch.getSubtitle() != null) {
                n = ch.getSubtitle().getTruncateLocation();
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
