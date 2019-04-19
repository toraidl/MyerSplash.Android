package com.juniperphoton.myersplash.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface DownloadItemDao {
    @Query("SELECT * FROM download_item ORDER BY create_time DESC")
    fun getAll(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM download_item WHERE id=:id")
    fun getById(id: String): LiveData<DownloadItem>

    @Query("SELECT * FROM download_item WHERE download_url=:url")
    fun getByUrl(url: String): LiveData<DownloadItem>

    @Query("UPDATE download_item SET status=:status WHERE id=:id")
    fun setStatusById(id: String, status: Int)

    @Query("UPDATE download_item SET status=:status WHERE download_url=:url")
    fun setStatusByUrl(url: String, status: Int)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_OK}, file_path=:path, progress=1 WHERE download_url=:url")
    fun setSuccess(url: String?, path: String?)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_FAILED}, file_path=null, progress=0 WHERE download_url=:url")
    fun setFailed(url: String?)

    @Query("UPDATE download_item SET progress=:progress WHERE download_url=:url")
    fun setProgress(url: String, progress: Int)

    @Query("DELETE FROM download_item WHERE id=:id")
    fun deleteById(id: String)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg items: DownloadItem)

    @Delete
    fun delete(item: DownloadItem)

    @Query("DELETE FROM download_item WHERE status=:status")
    fun deleteByStatus(status: Int)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_DOWNLOADING}, progress=0 WHERE id=:id")
    fun resetStatus(id: String)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_FAILED}, progress=0 WHERE status!=${DownloadItem.DOWNLOAD_STATUS_OK}")
    fun markAllFailed()
}