package com.juniperphoton.myersplash.contract

import com.juniperphoton.myersplash.presenter.MainListPresenter
import dagger.Component

@Component(modules = [(RepoModule::class)])
interface RepoComponent {
    fun inject(presenter: MainListPresenter)
}