package com.google.android.libraries.launcherclient

interface IScrollCallback {
    fun onOverlayScrollChanged(progress: Float)

    fun onServiceStateChanged(overlayAttached: Boolean)
}