package com.juniperphoton.myersplash.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.contract.DaggerRepoComponent
import com.juniperphoton.myersplash.contract.RepoModule
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.fragment.OnClickPhotoItemListener
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.presenter.MainListPresenter

class MainListFragmentAdapter(private var callback: OnClickPhotoItemListener?,
                              fm: androidx.fragment.app.FragmentManager
) : FragmentStatePagerAdapter(fm) {
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

    override fun getItem(position: Int): Fragment = MainListFragment()

    override fun getCount(): Int = 3
}