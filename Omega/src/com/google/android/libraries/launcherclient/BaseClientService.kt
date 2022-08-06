package com.google.android.libraries.launcherclient

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.saggitt.omega.preferences.OmegaPreferences.Companion.getInstance

open class BaseClientService(context: Context, flags: Int) : ServiceConnection {
    private var mConnected = false
    private val mContext: Context = context
    private val mFlags = flags
    private val mBridge: ServiceConnection = this

    fun connect(): Boolean {
        if (!mConnected) {
            try {
                val prefs = getInstance(mContext)
                if (prefs.feedProvider.onGetValue() != "") {
                    mConnected = mContext.bindService(
                        LauncherClient.getIntent(mContext, false),
                        mBridge,
                        mFlags
                    )
                }
            } catch (e: Throwable) {
                Log.e("LauncherClient", "Unable to connect to overlay service", e)
            }
        }
        return mConnected
    }

    fun disconnect() {
        if (mConnected) {
            mContext.unbindService(mBridge)
            mConnected = false
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}

    override fun onServiceDisconnected(name: ComponentName?) {}
}