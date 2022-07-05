package com.machiav3lli.fdroid.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.machiav3lli.fdroid.utility.extension.android.Android

abstract class ConnectionService<T : IBinder> : Service() {
    abstract override fun onBind(intent: Intent): T

    fun startSelf() {
        val intent = Intent(this, this::class.java)
        if (Android.sdk(26)) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
