/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.saggitt.omega.compose.screens

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.launcher3.R
import com.android.launcher3.Utilities

@Composable
fun LicenseScreen(isDark: Boolean) {
    ComposableWebView(url = "file:///android_asset/license.htm", isDark = isDark)
}

@Composable
fun ChangelogScreen(isDark: Boolean) {
    ComposableWebView(url = "file:///android_asset/changelog.htm", isDark = isDark)
}

@Composable
fun ComposableWebView(url: String, isDark: Boolean) {
    val cssFile = if (isDark) {
        "about_dark.css"
    } else {
        "about_light.css"
    }

    var webView: WebView? = null
    AndroidView(

        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    }
                }

                loadUrl(url)

                webView = this
            }
        }, update = {
            webView = it
        })

}

@Composable
fun TranslatorsScreen() {

    val translators: List<String> =
        (Utilities.readTextfileFromRawRes(R.raw.translators, LocalContext.current, "", "")
            .trim() + "\n\n").split("\n")
    val translatorsSize = translators.size - 2
    val languages: ArrayList<String> = arrayListOf()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        for (i in 0..translatorsSize step 4) {
            if (languages.size > 0 && !languages.contains(translators[i + 1])) {
                Divider(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                )
            }
            if (!languages.contains(translators[i + 1])) {
                languages.add(translators[i + 1])
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = translators[i + 1],
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )

                    Text(
                        text = translators[i] + " <" + translators[i + 2] + ">",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                ) {

                    Text(
                        text = translators[i] + " <" + translators[i + 2] + ">",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}