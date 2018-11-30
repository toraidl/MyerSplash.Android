package com.juniperphoton.myersplash.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LoadMoreListener(private val onLoadMore: (() -> Unit)? = null) {
    private var autoLoadMore = true

    fun attach(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager ?: return

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(list: RecyclerView, newState: Int) {
                super.onScrollStateChanged(list, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!autoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == layoutManager.itemCount) {
                        onLoadMore?.invoke()
                    }
                }
            }

            override fun onScrolled(list: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(list, dx, dy)
                if (autoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == layoutManager.itemCount) {
                    onLoadMore?.invoke()
                } else if (autoLoadMore) {
                    autoLoadMore = false
                }
            }
        })
    }

    private fun findLastVisibleItemPosition(layoutManager: RecyclerView.LayoutManager?): Int {
        return if (layoutManager is LinearLayoutManager) {
            layoutManager.findLastVisibleItemPosition()
        } else {
            -1
        }
    }
}