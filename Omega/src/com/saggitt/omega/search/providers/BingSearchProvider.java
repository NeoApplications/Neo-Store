/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.search.providers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.android.launcher3.util.PackageManagerHelper;
import com.saggitt.omega.search.SearchProvider;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class BingSearchProvider extends SearchProvider {
    private String PACKAGE = "com.microsoft.bing";
    private String PACKAGE_CORTANA = "com.microsoft.cortana";
    private String PACKAGE_ALEXA = "com.amazon.dee.app";

    public BingSearchProvider(Context context) {
        super(context);
    }

    @NotNull
    @Override
    public Drawable getIcon() {
        return getContext().getDrawable(R.drawable.ic_bing);
    }

    @NotNull
    @Override
    public String getName() {
        return getContext().getString(R.string.search_provider_bing);
    }

    @Override
    public boolean getSupportsAssistant() {
        return isCortanaInstalled() || isAlexaInstalled();
    }

    @Override
    public boolean getSupportsFeed() {
        return false;
    }

    @Override
    public boolean getSupportsVoiceSearch() {
        return true;
    }

    @Override
    public void startSearch(@NotNull Function1<? super Intent, Unit> callback) {
        Intent intent = (new Intent()).setClassName(PACKAGE, "com.microsoft.clients.bing.widget.WidgetSearchActivity").setPackage(PACKAGE);
        callback.invoke(intent);
    }

    @Override
    public void startVoiceSearch(@NotNull Function1<? super Intent, Unit> callback) {
        Intent intent = (new Intent(Intent.ACTION_SEARCH_LONG_PRESS)).setPackage(PACKAGE);
        callback.invoke(intent);
    }

    public void startAssistant(@NotNull Function1<? super Intent, Unit> callback) {
        Intent intent = new Intent();
        if (isCortanaInstalled()) {
            intent = new Intent()
                    .setClassName(PACKAGE_CORTANA, "com.microsoft.bing.dss.assist.AssistProxyActivity")
                    .setPackage(PACKAGE_CORTANA);
        } else {
            intent = new Intent(Intent.ACTION_ASSIST).setPackage(PACKAGE_ALEXA);
        }
        callback.invoke(intent);

    }

    @Override
    public boolean isAvailable() {
        return PackageManagerHelper.isAppEnabled(getContext().getPackageManager(), PACKAGE, 0);
    }

    @Override
    public Drawable getAssistantIcon() {
        return (isCortanaInstalled() ?
                getContext().getDrawable(R.drawable.ic_cortana) : getContext().getDrawable(R.drawable.ic_alexa));
    }

    @Override
    public Drawable getVoiceIcon() {
        Drawable voiceIcon = getContext().getDrawable(R.drawable.ic_qsb_mic);
        Objects.requireNonNull(voiceIcon).mutate().setTint(Color.parseColor("#00897B"));
        return voiceIcon;
    }

    @Override
    public Drawable getShadowAssistantIcon() {
        if (isCortanaInstalled()) {
            return wrapInShadowDrawable(getContext().getDrawable(R.drawable.ic_cortana));
        }
        return super.getShadowAssistantIcon();
    }

    private Boolean isCortanaInstalled() {
        return PackageManagerHelper.isAppEnabled(getContext().getPackageManager(), PACKAGE_CORTANA, 0);
    }

    private Boolean isAlexaInstalled() {
        return PackageManagerHelper.isAppEnabled(getContext().getPackageManager(), PACKAGE_ALEXA, 0);
    }
}
