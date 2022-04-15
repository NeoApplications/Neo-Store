package com.google.systemui.smartspace;

import static com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message.FormattedText.FormatParam.FormatParamArgs.SOMETHING2;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ResourceUtils;
import com.android.launcher3.icons.GraphicsUtils;
import com.android.launcher3.icons.ShadowGenerator;
import com.google.android.apps.nexuslauncher.utils.ColorManipulation;
import com.google.android.systemui.smartspace.SmartspaceProto.CardWrapper;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.ExpiryCriteria;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message.FormattedText;
import com.google.android.systemui.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Message.FormattedText.FormatParam;
import com.google.protobuf.ByteString;

public class SmartSpaceCardView {
    private final SmartSpaceCard mSmartSpaceCard;
    private final long mGsaUpdateTime;
    private final int mGsaVersionCode;
    private final boolean mIsGrayIconScale;
    private final boolean mPriority;
    private final long mPublishTime;
    private final Context mContext;
    private Bitmap mIcon;
    private final Intent mIntent;

    public SmartSpaceCardView(final Context context, SmartSpaceCard smartSpaceCard, Intent intent, boolean priority,
                              Bitmap icon, boolean isGrayIconScale, long publishTime, long gsaUpdateTime, int gsaVersionCode) {
        mContext = context.getApplicationContext();
        mSmartSpaceCard = smartSpaceCard;
        mPriority = priority;
        mIntent = intent;
        mIcon = icon;
        mPublishTime = publishTime;
        mGsaUpdateTime = gsaUpdateTime;
        mGsaVersionCode = gsaVersionCode;
        mIsGrayIconScale = isGrayIconScale;
    }

    static SmartSpaceCardView fromWrapper(Context context, CardWrapper cardWrapper, boolean priority) {
        if (cardWrapper != null) {
            try {
                Intent parseUri = TextUtils.isEmpty(cardWrapper.getCard().getTapAction().getIntent()) ?
                        null :
                        Intent.parseUri(cardWrapper.getCard().getTapAction().getIntent(), 0);

                Bitmap bitmap = cardWrapper.getIcon() == null ?
                        null :
                        BitmapFactory.decodeByteArray(cardWrapper.getIcon().toByteArray(), 0, cardWrapper.getIcon().size(), null);

                if (bitmap != null) {
                    ShadowGenerator shadowGenerator = new ShadowGenerator(
                            ResourceUtils.pxFromDp(48, context.getResources().getDisplayMetrics()));
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    shadowGenerator.recreateIcon(bitmap, new Canvas(newBitmap));
                    bitmap = newBitmap;
                }

                return new SmartSpaceCardView(context, cardWrapper.getCard(), parseUri, priority, bitmap, cardWrapper.getIsIconGrayscale(),
                        cardWrapper.getPublishTime(), cardWrapper.getGsaUpdateTime(), cardWrapper.getGsaVersionCode());
            } catch (Throwable e) {
                Log.e("SmartspaceCardView", "from proto", e);
            }
        }

        return null;
    }

    public static CardWrapper cQ(Context context, NewCardInfo card) {
        if (card == null) {
            return null;
        }
        CardWrapper.Builder builder = CardWrapper.newBuilder();
        final Bitmap ci = card.retrieveIcon(context);
        Bitmap cp;
        if (ci != null && builder.getIsIconGrayscale()) {
            if (card.mIsPrimary) {
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
        builder.setCard(card.mCard);
        builder.setPublishTime(card.mPublishTime);
        if (card.mPackageInfo != null) {
            builder.setGsaVersionCode(card.mPackageInfo.versionCode);
            builder.setGsaUpdateTime(card.mPackageInfo.lastUpdateTime);
        }
        return builder.build();
    }

    private FormattedText getFormattedText(final boolean b) {
        final Message message = getMessage();
        if (message != null) {
            FormattedText d;
            if (b) {
                d = message.getTitle();
            } else {
                d = message.getSubtitle();
            }
            return d;
        }
        return null;
    }

    private int getMinutesToEvent(FormatParam formatParam) {
        return (int) Math.ceil(getMillisToEvent(formatParam) / 60000.0);
    }

    private String[] getTextArgs(final FormatParam[] formatParamArr, final String s) {
        String[] array2 = new String[formatParamArr.length];
        String cr;
        for (int i = 0; i < array2.length; ++i) {
            switch (formatParamArr[i].getFormatParamArgs()) {

                case SOMETHING1:
                case SOMETHING2: {
                    array2[i] = getDurationText(formatParamArr[i]);
                    break;
                }

                case SOMETHING3: {
                    if (s != null && formatParamArr[i].getTruncateLocation() != 0) {
                        array2[i] = s;
                        break;
                    }
                    if (formatParamArr[i].getText() != null) {
                        cr = formatParamArr[i].getText();
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

    public long getExpiration() {
        ExpiryCriteria expiryCriteria = mSmartSpaceCard.getExpiryCriteria();
        if (expiryCriteria != null) {
            return expiryCriteria.getExpirationTimeMillis();
        }

        return 0;
    }

    public long getMillisToEvent(final FormatParam formatParam) {
        long timeToEvent;
        if (formatParam.getFormatParamArgs() == SOMETHING2) {
            timeToEvent = mSmartSpaceCard.getEventTimeMillis() + mSmartSpaceCard.getEventDurationMillis();
        } else {
            timeToEvent = mSmartSpaceCard.getEventDurationMillis();
        }
        return Math.abs(System.currentTimeMillis() - timeToEvent);
    }

    public void setIcon(Bitmap bitmap) {
        mIcon = bitmap;
    }

    public void performCardAction(View view) {
        if (this.mSmartSpaceCard.getTapAction() == null) {
            Log.e("SmartspaceCardView", "no tap action available: " + this);
            return;
        }
        Intent intent = new Intent(this.getIntent());
        Launcher launcher = Launcher.getLauncher(view.getContext());
        switch (this.mSmartSpaceCard.getTapAction().getActionType()) {
            case ACTION1: {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setSourceBounds(launcher.getViewBounds(view));
                intent.setPackage(com.saggitt.omega.util.Config.GOOGLE_QSB);
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

    private boolean hasParams(FormattedText formattedText) {
        if (!(formattedText == null || formattedText.getText() == null)) {
            FormatParam[] formatParamArr = formattedText.getFormatParamList().toArray(new FormatParam[0]);
            return formatParamArr.length > 0;
        }
        return false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > getExpiration();
    }

    public boolean hasMessage() {
        Message message = getMessage();
        return message != null && (hasParams(message.getTitle()) || hasParams(message.getSubtitle()));
    }

    private static Bitmap cP(final Bitmap bitmap, int n) {
        final Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(n, PorterDuff.Mode.SRC_IN));
        final Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(bitmap2).drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmap2;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public Intent getIntent() {
        return mIntent;
    }

    private Message getMessage() {
        final long currentTimeMillis = System.currentTimeMillis();
        final long eventTimeMillis = mSmartSpaceCard.getEventTimeMillis();
        final long n = mSmartSpaceCard.getEventTimeMillis() + mSmartSpaceCard.getEventDurationMillis();
        if (currentTimeMillis < eventTimeMillis && mSmartSpaceCard.getPreEvent() != null) {
            return mSmartSpaceCard.getPreEvent();
        }
        if (currentTimeMillis > n && mSmartSpaceCard.getPostEvent() != null) {
            return mSmartSpaceCard.getPostEvent();
        }
        if (mSmartSpaceCard.getDuringEvent() != null) {
            return mSmartSpaceCard.getDuringEvent();
        }
        return null;
    }

    private String getDurationText(FormatParam formatParam) {
        final Resources res = mContext.getResources();
        int minutesToEvent = getMinutesToEvent(formatParam);
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

    public String cC(final String s) {
        return substitute(true, s);
    }

    public String getTitle() {
        return substitute(true);
    }

    public String substitute(boolean b) {
        return substitute(b, null);
    }

    public String ca(boolean b) {
        return substitute(b, "");
    }

    public String cB(boolean b) {
        int i = 0;
        final FormatParam[] co = getFormattedText(b).getFormatParamList().toArray(new FormatParam[0]);
        while (i < co.length) {
            if (co[i].getTruncateLocation() != 0) {
                return co[i].getText();
            }
            ++i;
        }
        return "";
    }

    private String substitute(boolean b, final String str) {
        FormattedText formattedText = getFormattedText(b);
        if (formattedText != null) {
            String str2 = formattedText.getText();
            if (str2 != null) {
                return hasParams(formattedText) ?
                        String.format(str2, (Object[]) getTextArgs(formattedText
                                .getFormatParamList()
                                .toArray(new FormatParam[0]), str)) : str2;
            }
        }
        return "";
    }

    @NonNull
    @Override
    public String toString() {
        return "title:" + getTitle() +
                " expires:" + getExpiration() +
                " published:" + mPublishTime +
                " gsaVersion:" + mGsaVersionCode +
                " gsaUpdateTime: " + mGsaUpdateTime;
    }
}
