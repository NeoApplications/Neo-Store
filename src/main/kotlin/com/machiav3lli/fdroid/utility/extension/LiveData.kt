package com.machiav3lli.fdroid.utility.extension

import androidx.lifecycle.MediatorLiveData

class ManageableLiveData<T> : MediatorLiveData<T>() {
    var lastEdit: Long = 0L

    fun updateValue(value: T, updateTime: Long) {
        if (updateTime > lastEdit) {
            lastEdit = updateTime
            super.postValue(value)
        }
    }
}