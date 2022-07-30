package com.google.android.libraries.launcherclient

interface ISerializableScrollCallback : IScrollCallback {
    fun setPersistentFlags(flags: Int)
}