package com.machiav3lli.fdroid.utility.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

fun <T> Flow<T>.takeUntilSignal(signal: Flow<Boolean>): Flow<T> = channelFlow {
    val signalJob = launch {
        signal.distinctUntilChanged()
            .collect { if (it) close() } // Close the channel on signal
    }
    try {
        collect { send(it) }
    } finally {
        signalJob.cancel()
    }
}