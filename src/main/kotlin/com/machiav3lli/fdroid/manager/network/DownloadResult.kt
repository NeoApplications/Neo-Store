package com.machiav3lli.fdroid.manager.network

sealed class DownloadResult<T>(
    val progress: Long? = 0,
    val total: Long? = 0,
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T>(progress: Long? = null, total: Long? = null, data: T? = null) :
        DownloadResult<T>(progress, total, data)

    class Success<T>(data: T?) : DownloadResult<T>(data = data)
    class Error<T>(message: String, data: T? = null) :
        DownloadResult<T>(data = data, message = message)
}