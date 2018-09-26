package com.juniperphoton.myersplash.utils

import androidx.annotation.CallSuper
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.cloudservice.APIException
import io.reactivex.observers.DisposableObserver
import java.net.SocketTimeoutException

open class ResponseObserver<T> : DisposableObserver<T>() {
    override fun onComplete() {
        onFinish()
    }

    @CallSuper
    override fun onError(e: Throwable) {
        e.printStackTrace()
        when (e) {
            is SocketTimeoutException -> {
                ToastService.sendShortToast(R.string.timeout)
            }
            is APIException -> {
                ToastService.sendShortToast(R.string.failed_to_send_request)
            }
            else -> onUnknownError(e)
        }
        onFinish()
    }

    open fun onUnknownError(e: Throwable) = Unit

    override fun onNext(data: T) = Unit

    open fun onFinish() = Unit
}