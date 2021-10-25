package android.app;

import android.annotation.SuppressLint;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public final class Person implements Parcelable {
    public String getUri() {
        throw new RuntimeException("Stub");
    }

    public CharSequence getName() {
        throw new RuntimeException("Stub");
    }

    public Icon getIcon() {
        throw new RuntimeException("Stub");
    }

    public String getKey() {
        throw new RuntimeException("Stub");
    }

    public boolean isBot() {
        throw new RuntimeException("Stub");
    }

    public boolean isImportant() {
        throw new RuntimeException("Stub");
    }

    public String resolveToLegacyUri() {
        throw new RuntimeException("Stub");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub");
    }
}
