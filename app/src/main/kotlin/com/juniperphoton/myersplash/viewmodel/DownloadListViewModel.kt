package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.juniperphoton.myersplash.db.AppDatabase
import com.juniperphoton.myersplash.db.DownloadItemsRepo
import com.juniperphoton.myersplash.model.DownloadItem
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext

class DownloadListViewModel(application: Application
) : AndroidViewModel(application), CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val repository: DownloadItemsRepo

    val downloadItems: Flowable<List<DownloadItem>>
        get() = repository.downloadItems

    init {
        val dao = AppDatabase.instance.downloadItemDao()
        repository = DownloadItemsRepo(dao)
    }

    override fun onCleared() {
        cancel()
        super.onCleared()
    }

    suspend fun deleteByStatus(status: Int) = withContext(Dispatchers.IO) {
        repository.deleteByStatus(status)
    }

    suspend fun updateItemStatus(id: String, status: Int) {
        repository.updateStatus(id, status)
    }

    suspend fun resetItemStatus(id: String) {
        repository.resetStatus(id)
    }

    suspend fun deleteItem(id: String)  {
        repository.deleteById(id)
    }
}