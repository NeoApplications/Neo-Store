package com.google.systemui.smartspace;

import androidx.annotation.NonNull;

public class SmartSpaceData {
    public SmartSpaceCardView mCurrentCard;
    public SmartSpaceCardView mWeatherCard;

    public boolean hasCurrent() {
        return mCurrentCard != null;
    }

    public boolean hasWeather() {
        return mWeatherCard != null;
    }

    public long getExpiresAtMillis() {

        final long currentTimeMillis = System.currentTimeMillis();
        if (hasCurrent() && hasWeather()) {
            return Math.min(mCurrentCard.getExpiration(), mWeatherCard.getExpiration()) - currentTimeMillis;
        }
        if (hasCurrent()) {
            return mCurrentCard.getExpiration() - currentTimeMillis;
        }
        if (hasWeather()) {
            return mWeatherCard.getExpiration() - currentTimeMillis;
        }
        return 0;
    }

    public boolean handleExpire() {
        boolean z;
        if (!hasWeather() || !mWeatherCard.isExpired()) {
            z = false;
        } else {
            mWeatherCard = null;
            z = true;
        }
        if (!hasCurrent() || !mCurrentCard.isExpired()) {
            return z;
        }
        mCurrentCard = null;
        return true;
    }

    public void clear() {
        mWeatherCard = null;
        mCurrentCard = null;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + mCurrentCard + "," + mWeatherCard + "}";
    }
}
