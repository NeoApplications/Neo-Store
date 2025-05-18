@file:Suppress("PackageDirectoryMismatch")

package com.machiav3lli.fdroid.utils.extension.android

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.core.net.toUri
import com.machiav3lli.fdroid.utils.Utils

fun Cursor.asSequence(): Sequence<Cursor> {
    return generateSequence { if (moveToNext()) this else null }
}

fun SQLiteDatabase.execWithResult(sql: String) {
    rawQuery(sql, null).use { it.count }
}

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val PackageInfo.versionCodeCompat: Long
    get() = if (Android.sdk(Build.VERSION_CODES.P)) longVersionCode else @Suppress("DEPRECATION") versionCode.toLong()

fun Context.launchView(url: String) {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            url.toUri()
        )
    )
}

@Suppress("DEPRECATION")
val PackageInfo.signerSHA256Signatures: List<String>
    get() {
        return when {
            Android.sdk(Build.VERSION_CODES.P) -> this.signingInfo?.signingCertificateHistory
            else                               -> this.signatures
        }?.mapNotNull {
            it?.let(Utils::calculateSHA256).orEmpty()
        } ?: emptyList()
    }

object Android {
    val sdk: Int
        get() = Build.VERSION.SDK_INT

    val name: String
        get() = "Android ${Build.VERSION.RELEASE}"

    val platforms = Build.SUPPORTED_ABIS.toSet()

    val primaryPlatform: String?
        get() = Build.SUPPORTED_64_BIT_ABIS?.firstOrNull()
            ?: Build.SUPPORTED_32_BIT_ABIS?.firstOrNull()

    fun sdk(sdk: Int): Boolean {
        return Build.VERSION.SDK_INT >= sdk
    }

    object PackageManager {
        // GET_SIGNATURES should always present for getPackageArchiveInfo
        val signaturesFlag: Int
            get() = (if (sdk(Build.VERSION_CODES.P)) android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES else 0) or
                    @Suppress("DEPRECATION") android.content.pm.PackageManager.GET_SIGNATURES
    }
}
