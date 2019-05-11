package com.juniperphoton.myersplash.db

import com.juniperphoton.myersplash.model.DownloadItem
import io.reactivex.Flowable

class DetailImageRepo {
    private val dao = AppDatabase.instance.downloadItemDao()

    fun retrieveAssociatedItem(id: String): Flowable<DownloadItem> {
        return dao.getById(id)
    }

    suspend fun setStatusById(id: String, status: Int) {
        dao.setStatusById(id, status)
    }
}