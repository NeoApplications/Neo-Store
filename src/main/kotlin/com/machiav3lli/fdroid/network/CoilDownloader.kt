package com.machiav3lli.fdroid.network

import android.net.Uri
import com.machiav3lli.fdroid.HOST_ICON
import com.machiav3lli.fdroid.HOST_SCREENSHOT
import com.machiav3lli.fdroid.QUERY_ADDRESS
import com.machiav3lli.fdroid.QUERY_AUTHENTICATION
import com.machiav3lli.fdroid.QUERY_DEVICE
import com.machiav3lli.fdroid.QUERY_DPI
import com.machiav3lli.fdroid.QUERY_ICON
import com.machiav3lli.fdroid.QUERY_LOCALE
import com.machiav3lli.fdroid.QUERY_METADATA_ICON
import com.machiav3lli.fdroid.QUERY_PACKAGE_NAME
import com.machiav3lli.fdroid.QUERY_SCREENSHOT
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.Screenshot
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import okhttp3.Cache
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.io.File


object CoilDownloader {

    class Factory(cacheDir: File) : Call.Factory {
        private val cache = Cache(cacheDir, 50_000_000L)

        override fun newCall(request: Request): Call {
            return when (request.url.host) {
                HOST_ICON       -> {
                    val address = request.url.queryParameter(QUERY_ADDRESS)?.nullIfEmpty()
                    val authentication = request.url.queryParameter(QUERY_AUTHENTICATION)
                    val path = run {
                        val packageName =
                            request.url.queryParameter(QUERY_PACKAGE_NAME)?.nullIfEmpty()
                        val icon = request.url.queryParameter(QUERY_ICON)?.nullIfEmpty()
                        val metadataIcon =
                            request.url.queryParameter(QUERY_METADATA_ICON)?.nullIfEmpty()
                        val dpi = request.url.queryParameter(QUERY_DPI)?.nullIfEmpty()
                        when {
                            icon != null
                                 -> "${if (dpi != null) "icons-$dpi" else "icons"}/$icon"

                            packageName != null && metadataIcon != null
                                 -> "$packageName/$metadataIcon"

                            else -> null
                        }
                    }
                    if (address == null || path == null) {
                        Downloader.createCall(Request.Builder(), "", null)
                    } else {
                        Downloader.createCall(
                            request.newBuilder().url(
                                address.toHttpUrl()
                                    .newBuilder()
                                    .addPathSegments(path)
                                    .build()
                            ), authentication.orEmpty(), cache
                        )
                    }
                }

                HOST_SCREENSHOT -> {
                    val address = request.url.queryParameter(QUERY_ADDRESS)
                    val authentication = request.url.queryParameter(QUERY_AUTHENTICATION)
                    val packageName = request.url.queryParameter(QUERY_PACKAGE_NAME)
                    val locale = request.url.queryParameter(QUERY_LOCALE)
                    val device = request.url.queryParameter(QUERY_DEVICE)
                    val screenshot = request.url.queryParameter(QUERY_SCREENSHOT)
                    if (screenshot.isNullOrEmpty() || address.isNullOrEmpty()) {
                        Downloader.createCall(Request.Builder(), "", null)
                    } else {
                        Downloader.createCall(
                            request.newBuilder().url(
                                address.toHttpUrl()
                                    .newBuilder()
                                    .addPathSegment(packageName.orEmpty())
                                    .addPathSegment(locale.orEmpty())
                                    .addPathSegment(device.orEmpty())
                                    .addPathSegment(screenshot).build()
                            ),
                            authentication.orEmpty(), cache
                        )
                    }
                }

                else            -> {
                    Downloader.createCall(request.newBuilder(), "", null)
                }
            }
        }
    }
}

fun createScreenshotUri(
    repository: Repository,
    packageName: String,
    screenshot: Screenshot,
): Uri {
    return Uri.Builder().scheme("https").authority(HOST_SCREENSHOT)
        .appendQueryParameter(QUERY_ADDRESS, repository.address)
        .appendQueryParameter(QUERY_AUTHENTICATION, repository.authentication)
        .appendQueryParameter(QUERY_PACKAGE_NAME, packageName)
        .appendQueryParameter(QUERY_LOCALE, screenshot.locale)
        .appendQueryParameter(
            QUERY_DEVICE, when (screenshot.type) {
                Screenshot.Type.PHONE        -> "phoneScreenshots"
                Screenshot.Type.SMALL_TABLET -> "sevenInchScreenshots"
                Screenshot.Type.LARGE_TABLET -> "tenInchScreenshots"
            }
        )
        .appendQueryParameter(QUERY_SCREENSHOT, screenshot.path)
        .build()
}

fun createIconUri(
    packageName: String, icon: String, metadataIcon: String,
    address: String?, auth: String?,
): Uri = Uri.Builder().scheme("https").authority(HOST_ICON)
    .appendQueryParameter(QUERY_ADDRESS, address)
    .appendQueryParameter(QUERY_AUTHENTICATION, auth)
    .appendQueryParameter(QUERY_PACKAGE_NAME, packageName)
    .appendQueryParameter(QUERY_ICON, icon)
    .appendQueryParameter(QUERY_METADATA_ICON, metadataIcon)
    .build()
