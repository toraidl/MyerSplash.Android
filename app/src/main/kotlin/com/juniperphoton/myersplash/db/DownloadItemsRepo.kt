package com.juniperphoton.myersplash.db

import com.juniperphoton.myersplash.model.DownloadItem
import io.reactivex.Flowable

class DownloadItemsRepo(private val dao: DownloadItemDao) {
    val downloadItems: Flowable<List<DownloadItem>>
        get() = dao.getAll()

    fun deleteByStatus(status: Int) {
        dao.deleteByStatus(status)
    }

    fun updateStatus(id: String, status: Int) {
        dao.setStatusById(id, status)
    }

    fun resetStatus(id: String) {
        dao.resetStatus(id)
    }

    fun deleteById(id: String) {
        dao.deleteById(id)
    }
}