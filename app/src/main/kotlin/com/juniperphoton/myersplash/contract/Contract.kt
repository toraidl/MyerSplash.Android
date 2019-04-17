package com.juniperphoton.myersplash.contract

interface Contract {
    interface BasePresenter {
        fun start()
        fun stop()
    }

    interface BaseView<T> {
        var presenter: T
    }
}