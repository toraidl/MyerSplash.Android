package com.juniperphoton.myersplash.utils

import com.juniperphoton.myersplash.db.RealmCache
import com.juniperphoton.myersplash.model.DownloadItem

/**
 * Helper class for updating download item status in realm.
 */
@Suppress("unused")
object DownloadItemTransactionUtil {
    /**
     * Delete a managed download [item].
     */
    fun delete(item: DownloadItem) {
        RealmCache.getInstance().executeTransaction { realm ->
            val managedItem = realm.where(DownloadItem::class.java)
                    .equalTo(DownloadItem.ID_KEY, item.id).findFirst()
            managedItem?.deleteFromRealm()
        }
    }

    /**
     * Update download [status] of a download [item].
     * @param status see [DownloadItem.DownloadStatus].
     */
    fun updateStatus(item: DownloadItem, @DownloadItem.DownloadStatus status: Int) {
        RealmCache.getInstance().executeTransaction { realm ->
            val managedItem = realm.where(DownloadItem::class.java)
                    .equalTo(DownloadItem.ID_KEY, item.id).findFirst()
            if (managedItem != null) {
                managedItem.status = status
                if (status == DownloadItem.DOWNLOAD_STATUS_FAILED) {
                    managedItem.progress = 0
                }
            }
        }
    }

    /**
     * Update download [status] of given a [id].
     * @param status see [DownloadItem.DownloadStatus].
     */
    fun updateStatus(id: String, @DownloadItem.DownloadStatus status: Int) {
        RealmCache.getInstance().executeTransaction { realm ->
            val managedItem = realm.where(DownloadItem::class.java)
                    .equalTo(DownloadItem.ID_KEY, id).findFirst()
            if (managedItem != null) {
                managedItem.status = status
                if (status == DownloadItem.DOWNLOAD_STATUS_FAILED) {
                    managedItem.progress = 0
                }
            }
        }
    }
}