package com.juniperphoton.myersplash.db

import androidx.lifecycle.LiveData
import com.juniperphoton.myersplash.model.DownloadItem

class DetailImageRepo {
    private val dao = AppDatabase.instance.downloadItemDao()

    fun retrieveAssociatedItem(id: String): LiveData<DownloadItem> {
        return dao.getById(id)
    }

    fun setStatusById(id: String, status: Int) {
        dao.setStatusById(id, status)
    }
}