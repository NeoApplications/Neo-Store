package com.machiav3lli.fdroid.utility

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.Response

// TODO consider dropping
object CoroutineUtils {
    private class ManagedCancellable(private val cancel: suspend () -> Unit) {
        @Volatile
        var cancelled = false

        fun cancel() {
            cancelled = true
            cancel()
        }
    }

    private suspend fun <T, R : Any> managedSingle(
        create: suspend () -> T,
        cancel: suspend (T) -> Unit,
        execute: suspend (T) -> R,
    ): R {
        return withContext(Dispatchers.IO) {
            val task = create()
            val cancellable = ManagedCancellable {
                Thread.currentThread().interrupt()
                cancel(task)
            }
            try {
                withTimeout(Long.MAX_VALUE) {
                    if (!cancellable.cancelled) {
                        execute(task)
                    } else {
                        throw CancellationException()
                    }
                }
            } catch (e: Throwable) {
                if (!cancellable.cancelled) {
                    throw e
                } else {
                    throw CancellationException()
                }
            }
        }
    }

    suspend fun <R : Any> managedSingle(execute: suspend () -> R): R {
        return managedSingle({}, {}, { execute() })
    }

    suspend fun callSingle(create: suspend () -> Call): Response {
        return managedSingle(create, Call::cancel, Call::execute)
    }

    suspend fun <T : Any> querySingle(query: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            try {
                withTimeout(Long.MAX_VALUE) {
                    query()
                }
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    throw e
                } else {
                    throw CancellationException()
                }
            }
        }
    }
}
