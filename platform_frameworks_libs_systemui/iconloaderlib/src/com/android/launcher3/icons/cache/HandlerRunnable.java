/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.icons.cache;

import android.os.Handler;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A runnable that can be posted to a {@link Handler} which can be canceled.
 */
public class HandlerRunnable<T> implements Runnable {

    private final Handler mWorkerHandler;
    private final Supplier<T> mTask;

    private final Executor mCallbackExecutor;
    private final Consumer<T> mCallback;
    private final Runnable mEndRunnable;

    private boolean mEnded = false;
    private boolean mCanceled = false;

    public HandlerRunnable(Handler workerHandler, Supplier<T> task, Executor callbackExecutor,
                           Consumer<T> callback) {
        this(workerHandler, task, callbackExecutor, callback, () -> {
        });
    }

    public HandlerRunnable(Handler workerHandler, Supplier<T> task, Executor callbackExecutor,
                           Consumer<T> callback, Runnable endRunnable) {
        mWorkerHandler = workerHandler;
        mTask = task;
        mCallbackExecutor = callbackExecutor;
        mCallback = callback;
        mEndRunnable = endRunnable;
    }

    /**
     * Cancels this runnable from being run, only if it has not already run.
     */
    public void cancel() {
        mWorkerHandler.removeCallbacks(this);
        mCanceled = true;
        mCallbackExecutor.execute(this::onEnd);
    }

    @Override
    public void run() {
        T value = mTask.get();
        mCallbackExecutor.execute(() -> {
            if (!mCanceled) {
                mCallback.accept(value);
            }
            onEnd();
        });
    }

    private void onEnd() {
        if (!mEnded) {
            mEnded = true;
            mEndRunnable.run();
        }
    }
}
