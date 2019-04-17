package com.juniperphoton.myersplash.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.api.CloudService
import com.juniperphoton.myersplash.db.RealmCache
import com.juniperphoton.myersplash.extension.notifyFileUpdated
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.utils.*
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class DownloadService : Service(), CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private class LocalBinder : Binder()

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        private const val TAG = "IOService"
    }

    private var binder: LocalBinder = LocalBinder()

    // A map storing download url to downloading disposable object
    private val downloadUrlToJobMap = HashMap<String, Job>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Pasteur.info(TAG, "on start command")
        intent?.let {
            onHandleIntent(it)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    private fun onHandleIntent(intent: Intent) {
        val canceled = intent.getBooleanExtra(Params.CANCELED_KEY, false)
        val downloadUrl = intent.getStringExtra(Params.URL_KEY)
        val fileName = intent.getStringExtra(Params.NAME_KEY)
        val previewUrl = intent.getStringExtra(Params.PREVIEW_URI)
        val isUnsplash = intent.getBooleanExtra(Params.IS_UNSPLASH_WALLPAPER, true)
        if (!isUnsplash) {
            Toaster.sendShortToast(R.string.downloading)
        }

        val previewUri: Uri? = if (previewUrl.isNullOrEmpty()) null else {
            Uri.parse(previewUrl)
        }

        if (canceled) {
            Pasteur.d(TAG, "on handle intent cancelled")
            val job = downloadUrlToJobMap[downloadUrl]
            if (job != null) {
                job.cancel()
                NotificationUtil.cancelNotification(Uri.parse(downloadUrl))
                Toaster.sendShortToast(getString(R.string.cancelled_download))
            }
        } else {
            Pasteur.d(TAG, "on handle intent progress")
            downloadImage(downloadUrl, fileName, previewUri, isUnsplash)
        }
    }

    private fun downloadImage(url: String, fileName: String,
                              previewUri: Uri?, isUnsplash: Boolean) {
        val job = launch {
            val file = DownloadUtil.getFileToSave(fileName)
            try {
                val responseBody = CloudService.downloadPhoto(url)
                Pasteur.d(TAG, "outputFile download onNext, size=${responseBody.contentLength()}, thread: ${Thread.currentThread()}")
                val success = DownloadUtil.writeToFile(responseBody, file!!.path) {
                    RealmCache.getInstance().executeTransaction { realm ->
                        val downloadItem = realm.where(DownloadItem::class.java)
                                .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                        if (downloadItem != null) {
                            downloadItem.progress = it
                        }
                    }
                } != null

                if (!success) {
                    onError(url, fileName, previewUri)
                } else {
                    onSuccess(url, file, previewUri, isUnsplash)
                }

                Pasteur.d(TAG, getString(R.string.completed))
            } catch (e: Exception) {
                e.printStackTrace()
                Pasteur.d(TAG, "on handle intent error ${e.message}, url $url, thread ${Thread.currentThread()}")
                onError(url, fileName, null)
            }
        }
        downloadUrlToJobMap[url] = job
    }

    private fun onSuccess(url: String, file: File, previewUri: Uri?, isUnsplash: Boolean) {
        Pasteur.d(TAG, "output file:" + file.absolutePath)

        val newFile = File("${file.path.replace(" ", "")}.jpg")
        file.renameTo(newFile)

        Pasteur.d(TAG, "renamed file:" + newFile.absolutePath)
        newFile.notifyFileUpdated(App.instance)

        val realm = RealmCache.getInstance()
        val downloadItem = realm.where(DownloadItem::class.java)
                .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
        if (downloadItem != null) {
            realm.executeTransaction {
                downloadItem.status = DownloadItem.DOWNLOAD_STATUS_OK
                downloadItem.filePath = newFile.path
            }
        }

        NotificationUtil.showCompleteNotification(Uri.parse(url), previewUri,
                if (isUnsplash) null else newFile.absolutePath)
    }

    private fun onError(url: String, fileName: String, previewUri: Uri?) {
        NotificationUtil.showErrorNotification(Uri.parse(url), fileName,
                url, previewUri)
        RealmCache.getInstance().executeTransaction { realm ->
            val downloadItem = realm.where(DownloadItem::class.java)
                    .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
            if (downloadItem != null) {
                downloadItem.status = DownloadItem.DOWNLOAD_STATUS_FAILED
            }
        }
    }
}
