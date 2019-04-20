package com.juniperphoton.myersplash.presenter

import com.juniperphoton.myersplash.api.CloudService
import com.juniperphoton.myersplash.contract.MainContract
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.utils.Pasteur
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

open class MainListPresenter : MainContract.MainPresenter, CoroutineScope by MainScope() {
    companion object {
        const val DEFAULT_PAGING = 1
        private const val TAG = "MainListPresenter"
    }

    private var nextPage: Int = DEFAULT_PAGING
    private var refreshing: Boolean = false

    @Inject
    override lateinit var category: UnsplashCategory
    @Inject
    lateinit var mainView: MainContract.MainView

    override var query: String? = null

    override fun stop() {
        cancel()
    }

    override fun start() = Unit

    override fun search(query: String) {
        Pasteur.d(TAG, "on search:$query")
        if (!mainView.isBusyRefreshing) {
            this.query = query
            refresh()
        }
    }

    override fun onReceivedScrollToTopEvent(event: ScrollToTopEvent) {
        if (event.id == category.id) {
            mainView.scrollToTop()
            if (event.refresh) {
                refresh()
            }
        }
    }

    override fun loadMore() {
        loadPhotoList(++nextPage)
    }

    override fun reloadList() {
        loadPhotoList(nextPage)
    }

    override fun refresh() {
        loadPhotoList(DEFAULT_PAGING)
    }

    private fun refreshFinished() {
        refreshing = false
        mainView.setRefreshing(false)
    }

    // We ignore the returned Job because we have plus the scope with the [job] member,
    // so when [job] is cancelled, the job returned here is cancelled too.
    private fun loadPhotoList(next: Int) = launch {
        nextPage = next
        refreshing = true

        mainView.setRefreshing(next == DEFAULT_PAGING)

        val category = category
        try {
            val list = when (category.id) {
                UnsplashCategory.NEW_CATEGORY_ID ->
                    CloudService.getPhotos(category.requestUrl!!, next)
                UnsplashCategory.FEATURED_CATEGORY_ID ->
                    CloudService.getFeaturedPhotos(category.requestUrl!!, next)
                UnsplashCategory.HIGHLIGHTS_CATEGORY_ID ->
                    CloudService.getHighlightsPhotos(next)
                UnsplashCategory.SEARCH_ID ->
                    CloudService.searchPhotos(category.requestUrl!!, next, query!!)
                else -> throw IllegalArgumentException("unknown category id")
            }
            mainView.refreshList(list, nextPage)
            refreshFinished()
        } catch (e: Exception) {
            mainView.updateNoItemVisibility()
            mainView.setRefreshing(false)
        }
    }
}