package com.juniperphoton.myersplash.utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.juniperphoton.myersplash.App

object Toaster {
    private var handler = Handler(Looper.getMainLooper())

    fun sendShortToast(str: String?) {
        if (str == null) {
            return
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            handler.post { sendToastInternal(str) }
        } else {
            sendToastInternal(str)
        }
    }

    fun sendShortToast(strId: Int) {
        if (strId == 0) {
            return
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            handler.post { sendToastInternal(App.instance.getString(strId)) }
        } else {
            handler.post { sendToastInternal(App.instance.getString(strId)) }
        }
    }

    @SuppressLint("InflateParams")
    private fun sendToastInternal(str: String?) {
        Toast.makeText(App.instance, str, Toast.LENGTH_SHORT).show()
    }
}