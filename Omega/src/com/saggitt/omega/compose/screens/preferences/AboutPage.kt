/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.compose.screens.preferences

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import coil.annotation.ExperimentalCoilApi
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.saggitt.omega.compose.components.ContributorRow
import com.saggitt.omega.compose.components.ItemLink
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PagePreference
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.compose.objects.PageItem
import com.saggitt.omega.theme.kaushanScript
import com.saggitt.omega.util.Config
import java.io.InputStream

@OptIn(ExperimentalCoilApi::class)
fun NavGraphBuilder.aboutGraph(route: String) {
    preferenceGraph(route, { AboutPage() }) { subRoute ->
        licenseGraph(route = subRoute(Routes.LICENSE))
        translatorsGraph(route = subRoute(Routes.TRANSLATORS))
        changelogGraph(route = subRoute(Routes.CHANGELOG))
    }
}

fun NavGraphBuilder.licenseGraph(route: String) {
    preferenceGraph(route, { LicenseScreen() })
}

fun NavGraphBuilder.translatorsGraph(route: String) {
    preferenceGraph(route, { TranslatorsScreen() })
}

fun NavGraphBuilder.changelogGraph(route: String) {
    preferenceGraph(route, { ChangelogScreen() })
}

@ExperimentalCoilApi
@Composable
fun AboutPage() {
    ViewWithActionBar(
        title = stringResource(R.string.title__general_about),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16f))

                ) {
                    ResourcesCompat.getDrawable(
                        LocalContext.current.resources,
                        R.mipmap.ic_launcher,
                        LocalContext.current.theme
                    )?.let { drawable ->
                        val bitmap = Bitmap.createBitmap(
                            drawable.intrinsicWidth,
                            drawable.intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.requiredSize(84.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.derived_app_name),
                    fontFamily = kaushanScript,
                    fontWeight = FontWeight.Normal,
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(id = R.string.app_version) + ": "
                            + BuildConfig.VERSION_NAME + " ( Build " + BuildConfig.VERSION_CODE + " )",
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(id = R.string.app_id) + ": " + BuildConfig.APPLICATION_ID,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                links.map { link ->
                    ItemLink(
                        iconResId = link.iconResId,
                        label = stringResource(id = link.labelResId),
                        modifier = Modifier.weight(1f),
                        url = link.url
                    )
                }
            }

            PreferenceGroup(heading = stringResource(id = R.string.about_team)) {
                val size = contributors.size
                contributors.forEachIndexed { i, it ->
                    ContributorRow(
                        nameId = it.name,
                        roleId = it.descriptionRes,
                        photoUrl = it.photoUrl,
                        url = it.webpage,
                        index = i,
                        groupSize = size
                    )
                    if (i + 1 < size) Spacer(modifier = Modifier.height(2.dp))
                }
            }

            PreferenceGroup(heading = stringResource(id = R.string.about_translators_group)) {
                ContributorRow(
                    nameId = R.string.contributor2,
                    roleId = R.string.contributor_role,
                    photoUrl = "https://avatars.githubusercontent.com/u/69337602",
                    url = "https://github.com/nonaybay",
                    index = 0,
                    groupSize = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                PagePreference(
                    titleId = PageItem.AboutTranslators.titleId,
                    iconId = PageItem.AboutTranslators.iconId,
                    route = PageItem.AboutTranslators.route,
                    index = 1,
                    groupSize = 2
                )
            }

            PreferenceGroup(
                heading = stringResource(id = R.string.category__about_licenses),
                prefs = listOf(PageItem.AboutLicense, PageItem.AboutChangelog)
            )
        }
    }
}

private data class Link(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int,
    val url: String
)

private data class TeamMember(
    @StringRes val name: Int,
    @StringRes val descriptionRes: Int,
    val photoUrl: String,
    val webpage: String
)

private val links = listOf(
    Link(
        iconResId = R.drawable.ic_github,
        labelResId = R.string.about_source,
        url = "https://github.com/NeoApplications/Neo-Launcher"
    ),
    Link(
        iconResId = R.drawable.ic_channel,
        labelResId = R.string.about_channel,
        url = "https://t.me/neo_applications"
    ),
    Link(
        iconResId = R.drawable.ic_community,
        labelResId = R.string.about_community,
        url = "https://t.me/neo_launcher"
    )
)

private val contributors = listOf(
    TeamMember(
        name = R.string.author,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/6044050",
        webpage = "https://github.com/saulhdev"
    ),
    TeamMember(
        name = R.string.contributor1,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/40302595",
        webpage = "https://github.com/machiav3lli"
    )
)

@Composable
fun LicenseScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.about_open_source),
    ) {
        ComposableWebView(url = "file:///android_asset/license.htm")
    }
}

@Composable
fun ChangelogScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.title__about_changelog),
    ) {
        ComposableWebView(url = "file:///android_asset/changelog.htm")
        Spacer(modifier = Modifier.requiredHeight(50.dp))
    }
}

@Composable
fun ComposableWebView(url: String) {

    val cssFile = when (Config.getCurrentTheme(LocalContext.current)) {
        Config.THEME_BLACK -> "black.css"
        Config.THEME_DARK -> "dark.css"
        else -> "light.css"
    }
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        if (url.startsWith("file:///android_asset")) {
                            try {
                                settings.javaScriptEnabled = true
                                val inputStream: InputStream = context.assets.open(cssFile)
                                val buffer = ByteArray(inputStream.available())
                                inputStream.read(buffer)
                                inputStream.close()
                                val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
                                loadUrl(
                                    "javascript:(function() { " +
                                            "var head  = document.getElementsByTagName('head')[0];" +
                                            "var style = document.createElement('style');" +
                                            "style.type = 'text/css';" +
                                            "style.innerHTML =  window.atob('" + encoded + "');" +
                                            "head.appendChild(style);" +
                                            "})()"
                                )
                                settings.javaScriptEnabled = false
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        super.onPageFinished(view, url)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        if (url.contains("file://")) {
                            view.loadUrl(url)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            try {
                                ContextCompat.startActivity(context, intent, null)
                            } catch (e: ActivityNotFoundException) {
                                view.loadUrl(url)
                            }
                        }
                        return true
                    }
                }
            }
        },
        update = { webView -> webView.loadUrl(url) }
    )
}

@Composable
fun TranslatorsScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.about_translators),
    ) {
        ComposableWebView(url = "file:///android_asset/translators.htm")
        Spacer(modifier = Modifier.requiredHeight(50.dp))
    }
}