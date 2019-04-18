package com.juniperphoton.myersplash.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.juniperphoton.myersplash.contract.Contract
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