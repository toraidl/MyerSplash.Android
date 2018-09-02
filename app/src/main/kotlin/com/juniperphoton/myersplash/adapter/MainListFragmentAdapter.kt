package com.juniperphoton.myersplash.adapter

import android.graphics.RectF
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.View
import android.view.ViewGroup
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.data.DaggerRepoComponent
import com.juniperphoton.myersplash.data.MainListPresenter
import com.juniperphoton.myersplash.data.RepoModule
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashImage

class MainListFragmentAdapter(private var callback: ((RectF, UnsplashImage, View) -> Unit)?,
                              fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val o = super.instantiateItem(container, position)
        if (o is MainListFragment) {
            inject(o, position)
        }
        return o
    }

    private fun inject(fragment: MainListFragment, position: Int) {
        val presenter = MainListPresenter()
        val component = DaggerRepoComponent.builder()
                .repoModule(RepoModule(App.instance, position, fragment)).build()
        component.inject(presenter)

        fragment.presenter = presenter
        fragment.onClickPhotoItem = callback
    }

    override fun getItem(position: Int): Fragment = MainListFragment()

    override fun getCount(): Int = 3
}