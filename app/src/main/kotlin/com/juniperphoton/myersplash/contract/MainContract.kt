package com.juniperphoton.myersplash.contract

import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage

interface MainContract {
    interface MainPresenter : Contract.BasePresenter {
        var category: UnsplashCategory
        var query: String?

        fun refresh()
        fun reloadList()
        fun loadMore()
        fun search(query: String)
        fun onReceivedScrollToTopEvent(event: ScrollToTopEvent)
    }

    interface MainView : Contract.BaseView<MainPresenter?> {
        val isBusyRefreshing: Boolean

        fun search(query: String)
        fun refreshList(images: MutableList<UnsplashImage>, next: Int)
        fun clearData()
        fun setRefreshing(refreshing: Boolean)
        fun updateNoItemVisibility()
        fun scrollToTop()
        fun showToast(text: String)
        fun showToast(textId: Int)
        fun registerEvent()
        fun unregisterEvent()
    }
}