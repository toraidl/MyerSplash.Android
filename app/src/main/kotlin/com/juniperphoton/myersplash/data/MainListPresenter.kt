package com.juniperphoton.myersplash.data

import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.model.UnsplashImageFactory
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.utils.ResponseObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

open class MainListPresenter : MainContract.MainPresenter {
    companion object {
        const val DEFAULT_PAGING = 1
        private const val TAG = "MainListPresenter"
    }

    private var nextPage: Int = DEFAULT_PAGING
    private var refreshing: Boolean = false
    private var disposableList = CompositeDisposable()

    @Inject
    override lateinit var category: UnsplashCategory
    @Inject
    lateinit var mainView: MainContract.MainView
    @Inject
    lateinit var preferenceRepo: PreferenceRepo

    override var query: String? = null

    override fun stop() {
        disposableList.dispose()
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

    private fun setSignalOfEnd() {
        refreshing = false
        mainView.setRefreshing(false)
    }

    private fun getObserver(): DisposableObserver<MutableList<UnsplashImage>> {
        return object : ResponseObserver<MutableList<UnsplashImage>>() {
            override fun onFinish() {
                setSignalOfEnd()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                mainView.updateNoItemVisibility()
                mainView.setRefreshing(false)
            }

            override fun onNext(data: MutableList<UnsplashImage>) {
                if (category.id == UnsplashCategory.NEW_CATEGORY_ID
                        && nextPage == DEFAULT_PAGING
                        && preferenceRepo.getBoolean(App.instance.getString(R.string.preference_key_recommendation), true)) {
                    data.add(0, UnsplashImageFactory.createTodayImage())
                }
                mainView.refreshList(data, nextPage)
            }
        }
    }

    private fun loadPhotoList(next: Int) {
        nextPage = next
        refreshing = true

        mainView.setRefreshing(next == DEFAULT_PAGING)

        category.let {
            val o = when (it.id) {
                UnsplashCategory.NEW_CATEGORY_ID ->
                    CloudService.getPhotos(it.requestUrl!!, next)
                UnsplashCategory.FEATURED_CATEGORY_ID ->
                    CloudService.getFeaturedPhotos(it.requestUrl!!, next)
                UnsplashCategory.HIGHLIGHTS_CATEGORY_ID ->
                    CloudService.getHighlightsPhotos(next)
                UnsplashCategory.SEARCH_ID ->
                    CloudService.searchPhotos(it.requestUrl!!, next, query!!)
                else -> throw IllegalArgumentException("unknown category id")
            }
            disposableList.add(o.subscribeWith(getObserver()))
        }
    }
}