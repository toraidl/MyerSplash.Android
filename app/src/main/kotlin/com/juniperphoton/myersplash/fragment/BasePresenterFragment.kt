package com.juniperphoton.myersplash.fragment

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.View
import com.juniperphoton.myersplash.data.Contract
import com.juniperphoton.myersplash.utils.Pasteur

abstract class BasePresenterFragment<T : Contract.BasePresenter?> : Fragment(), Contract.BaseView<T?> {
    companion object {
        const val TAG = "BasePresenterFragment"
    }

    override var presenter: T? = null
        set(value) {
            Pasteur.info(TAG, "set presenter $activity")
            field = value
        }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
    }
}