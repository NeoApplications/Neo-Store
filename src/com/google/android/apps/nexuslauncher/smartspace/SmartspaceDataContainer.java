package com.google.android.apps.nexuslauncher.smartspace;

public class SmartspaceDataContainer {
    public SmartspaceCard weatherCard;
    public SmartspaceCard dataCard;

    public SmartspaceDataContainer() {
        weatherCard = null;
        dataCard = null;
    }

    public boolean isWeatherAvailable() {
        return weatherCard != null;
    }

    public boolean isDataAvailable() {
        return dataCard != null;
    }

    public long cT() {
        final long currentTimeMillis = System.currentTimeMillis();
        if (isDataAvailable() && isWeatherAvailable()) {
            return Math.min(dataCard.cF(), weatherCard.cF()) - currentTimeMillis;
        }
        if (isDataAvailable()) {
            return dataCard.cF() - currentTimeMillis;
        }
        if (isWeatherAvailable()) {
            return weatherCard.cF() - currentTimeMillis;
        }
        return 0L;
    }

    public boolean clearAll() {
        final boolean b = true;
        boolean b2 = false;
        if (isWeatherAvailable() && weatherCard.cM()) {
            weatherCard = null;
            b2 = b;
        }
        if (isDataAvailable() && dataCard.cM()) {
            dataCard = null;
            b2 = b;
        }
        return b2;
    }

    public String toString() {
        return "{" + dataCard + "," + weatherCard + "}";
    }
}
