package com.juniperphoton.myersplash.adapter

import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.data.DaggerRepoComponent
import com.juniperphoton.myersplash.data.MainListPresenter
import com.juniperphoton.myersplash.data.RepoModule
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage

class MainListFragmentAdapter(private var callback: ((RectF, UnsplashImage, View) -> Unit)?,
                              fm: androidx.fragment.app.FragmentManager
) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val o = super.instantiateItem(container, position)
        if (o is MainListFragment) {
            val categoryId = when (position) {
                0 -> UnsplashCategory.NEW_CATEGORY_ID
                1 -> UnsplashCategory.FEATURED_CATEGORY_ID
                else -> UnsplashCategory.HIGHLIGHTS_CATEGORY_ID
            }
            inject(o, categoryId)
        }
        return o
    }

    private fun inject(fragment: MainListFragment, categoryId: Int) {
        val presenter = MainListPresenter()
        val component = DaggerRepoComponent.builder()
                .repoModule(RepoModule(App.instance, categoryId, fragment)).build()
        component.inject(presenter)

        fragment.presenter = presenter
        fragment.onClickPhotoItem = callback
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment = MainListFragment()

    override fun getCount(): Int = 3
}