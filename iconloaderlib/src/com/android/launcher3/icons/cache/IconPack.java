package com.android.launcher3.icons.cache;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import androidx.palette.graphics.Palette;

import com.android.launcher3.icons.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IconPack {
    /*
    Useful Links:
    https://github.com/teslacoil/Example_NovaTheme
    http://stackoverflow.com/questions/7205415/getting-resources-of-another-application
    http://stackoverflow.com/questions/3890012/how-to-access-string-resource-from-another-application
     */
    private String packageName;
    private Context mContext;
    private Map<String, String> mIconPackResources;
    private List<String> mIconBackStrings;
    private List<Drawable> mIconBackList;
    private Drawable mIconUpon, mIconMask;
    private Resources mLoadedIconPackResource;
    private float mIconScale;

    public IconPack(Context context, String packageName) {
        this.packageName = packageName;
        mContext = context;
    }

    public void setIcons(Map<String, String> iconPackResources, List<String> iconBackStrings) {
        mIconPackResources = iconPackResources;
        mIconBackStrings = iconBackStrings;
        mIconBackList = new ArrayList<Drawable>();
        try {
            mLoadedIconPackResource = mContext.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            // must never happen cause itys checked already in the provider
            return;
        }
        mIconMask = getDrawableForName(IconPackProvider.ICON_MASK_TAG);
        mIconUpon = getDrawableForName(IconPackProvider.ICON_UPON_TAG);
        for (int i = 0; i < mIconBackStrings.size(); i++) {
            String backIconString = mIconBackStrings.get(i);
            Drawable backIcon = getDrawableWithName(backIconString);
            if (backIcon != null) {
                mIconBackList.add(backIcon);
            }
        }
        String scale = mIconPackResources.get(IconPackProvider.ICON_SCALE_TAG);
        if (scale != null) {
            try {
                mIconScale = Float.valueOf(scale);
            } catch (NumberFormatException e) {
            }
        }
    }

    public Drawable getIcon(LauncherActivityInfo info, Drawable appIcon, CharSequence appLabel) {
        return getIcon(info.getComponentName(), appIcon, appLabel);
    }

    public Drawable getIcon(ActivityInfo info, Drawable appIcon, CharSequence appLabel) {
        return getIcon(new ComponentName(info.packageName, info.name), appIcon, appLabel);
    }

    public Drawable getIcon(ComponentName name, Drawable appIcon, CharSequence appLabel) {
        return getDrawable(name.flattenToString(), appIcon, appLabel);
    }

    public Drawable getIcon(String packageName, Drawable appIcon, CharSequence appLabel) {
        return getDrawable(packageName, appIcon, appLabel);
    }

    private static Bitmap pad(Bitmap src) {
        Bitmap ret = Bitmap.createBitmap(src.getWidth() + 150, src.getHeight() + 150, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(ret);
        c.drawARGB(0x0, 0xFF, 0xFF, 0xFF);
        c.drawBitmap(src, (c.getWidth() - src.getWidth()) >> 1, (c.getHeight() - src.getHeight()) >> 1, null);
        return ret;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private Drawable getDrawable(String name, Drawable appIcon, CharSequence appLabel) {
        Drawable d = getDrawableForName(name);
        if (d == null && appIcon != null) {
            d = appIcon;
        }
        return wrapAdaptiveIcon(d, mContext);
    }

    public static Drawable wrapAdaptiveIcon(Drawable d, Context context) {
        SharedPreferences sharedPrefs = Utilities.getPrefs(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !(d instanceof AdaptiveIconDrawable)
                && sharedPrefs.getBoolean("prefs_wrapAdaptive", false)) {
            assert d != null;
            Bitmap b = drawableToBitmap(d);
            // Already running on UI_HELPER, no need to async this.
            Palette p = (new Palette.Builder(b)).generate();
            boolean makeColoredBackgrounds = sharedPrefs.getBoolean("pref_makeColoredBackgrounds", false);
            ColorDrawable backgroundColor = makeColoredBackgrounds ? new ColorDrawable(p.getDominantColor(Color.WHITE)) : new ColorDrawable(Color.WHITE);
            d = new AdaptiveIconDrawable(backgroundColor, new BitmapDrawable(pad(b)));
        }
        return d;
    }

    private Drawable getIconBackFor(CharSequence tag) {
        if (mIconBackList != null && mIconBackList.size() != 0) {
            if (mIconBackList.size() == 1) {
                return mIconBackList.get(0);
            }
            try {
                Drawable back = mIconBackList.get((tag.hashCode() & 0x7fffffff) % mIconBackList.size());
                return back;
            } catch (ArrayIndexOutOfBoundsException e) {
                return mIconBackList.get(0);
            }
        }
        return null;
    }

    private int getResourceIdForDrawable(String resource) {
        int resId = mLoadedIconPackResource.getIdentifier(resource, "drawable", packageName);
        return resId;
    }

    private Drawable getDrawableForName(String name) {
        String item = mIconPackResources.get(name);
        if (!TextUtils.isEmpty(item)) {
            int id = getResourceIdForDrawable(item);
            if (id != 0) {
                return mLoadedIconPackResource.getDrawable(id);
            }
        }
        return null;
    }

    private Drawable getDrawableWithName(String name) {
        int id = getResourceIdForDrawable(name);
        if (id != 0) {
            return mLoadedIconPackResource.getDrawable(id);
        }
        return null;
    }

    private BitmapDrawable getBitmapDrawable(Drawable image) {
        if (image instanceof BitmapDrawable) {
            return (BitmapDrawable) image;
        }
        final Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,
                Paint.FILTER_BITMAP_FLAG));

        Bitmap bmResult = Bitmap.createBitmap(image.getIntrinsicWidth(), image.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bmResult);
        image.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        image.draw(canvas);
        return new BitmapDrawable(mLoadedIconPackResource, bmResult);
    }

    public int getTotalIcons() {
        return mIconBackStrings.size();
    }
}
