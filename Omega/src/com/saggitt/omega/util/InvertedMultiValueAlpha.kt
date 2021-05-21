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
package com.saggitt.omega.util

import android.util.Property
import java.util.function.Consumer

/**
 * Utility class to handle separating a single value as a factor of multiple values
 */
class InvertedMultiValueAlpha(private val mConsumer: Consumer<Float>, size: Int) {
    private val mMyProperties: Array<InvertedAlphaProperty?> = arrayOfNulls(size)
    private var mValidMask: Int
    fun getProperty(index: Int): InvertedAlphaProperty? {
        return mMyProperties[index]
    }

    inner class InvertedAlphaProperty internal constructor(private val mMyMask: Int) {
        var mValue = 1f

        // Factor of all other alpha channels, only valid if mMyMask is present in mValidMask.
        private var mOthers = 1f
        // Our cache value is not correct, recompute it.

        // Since we have changed our value, all other caches except our own need to be
        // recomputed. Change mValidMask to indicate the new valid caches (only our own).
        var value: Float
            get() = 1 - mValue
            set(value) {
                var mValue = value
                mValue = 1 - mValue
                if (this.mValue == mValue) {
                    return
                }
                if (mValidMask and mMyMask == 0) {
                    // Our cache value is not correct, recompute it.
                    mOthers = 1f
                    for (prop in mMyProperties) {
                        if (prop !== this) {
                            mOthers *= prop!!.mValue
                        }
                    }
                }

                // Since we have changed our value, all other caches except our own need to be
                // recomputed. Change mValidMask to indicate the new valid caches (only our own).
                mValidMask = mMyMask
                this.mValue = mValue
                mConsumer.accept(1 - mOthers * this.mValue)
            }
    }

    companion object {
        val VALUE: Property<InvertedAlphaProperty, Float> =
            object : Property<InvertedAlphaProperty, Float>(java.lang.Float.TYPE, "value") {
                override fun get(alphaProperty: InvertedAlphaProperty): Float {
                    return 1 - alphaProperty.mValue
                }

                override fun set(`object`: InvertedAlphaProperty, value: Float) {
                    `object`.value = value
                }
            }
    }

    init {
        mValidMask = 0
        for (i in 0 until size) {
            val myMask = 1 shl i
            mValidMask = mValidMask or myMask
            mMyProperties[i] = InvertedAlphaProperty(myMask)
        }
    }
}