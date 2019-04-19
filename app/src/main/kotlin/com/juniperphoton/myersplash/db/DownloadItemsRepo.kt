package com.juniperphoton.myersplash.db

import androidx.lifecycle.LiveData
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.DownloadItemDao

class DownloadItemsRepo(private val dao: DownloadItemDao) {
    val downloadItems: LiveData<List<DownloadItem>>
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