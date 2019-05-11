package com.juniperphoton.myersplash.extension

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.getDistinct(): LiveData<T> {
    return MediatorLiveData<T>().apply data@{
        addSource(this, object : Observer<T> {
            private var initialized = false
            private var lastObj: T? = null

            override fun onChanged(obj: T?) {
                if (!initialized) {
                    initialized = true
                    lastObj = obj
                    this@data.postValue(lastObj)
                } else if ((obj == null && lastObj != null)
                        || obj != lastObj) {
                    lastObj = obj
                    this@data.postValue(lastObj)
                }
            }
        })
    }
}