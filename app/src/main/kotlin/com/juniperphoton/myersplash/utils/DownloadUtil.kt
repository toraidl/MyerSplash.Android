package com.juniperphoton.myersplash.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.event.DownloadStartedEvent
import com.juniperphoton.myersplash.extension.usingWifi
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.service.DownloadService
import io.realm.Sort
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.util.*

@Suppress("unused_parameter")
object DownloadUtil {
    private const val TAG = "DownloadUtil"

    /**
     * Write [body] to a file of [fileUri].
     * @param onProgress will be invoked when the progress has been updated.
     */
    fun writeToFile(body: ResponseBody, fileUri: String, onProgress: ((Int) -> Unit)?): File? {
        return try {
            val fileToSave = File(fileUri)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val startTime = Date().time

                inputStream = body.byteStream()
                outputStream = FileOutputStream(fileToSave)

                val buffer = ByteArray(4096)

                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                var progressToReport = 0

                while (true) {
                    val read = inputStream!!.read(buffer)
                    if (read == -1) {
                        break
                    }

                    outputStream.write(buffer, 0, read)
                    fileSizeDownloaded += read.toLong()

                    val progress = (fileSizeDownloaded / fileSize.toDouble() * 100).toInt()
                    if (progress - progressToReport >= 5) {
                        progressToReport = progress
                        onProgress?.invoke(progressToReport)
                    }
                }
                val endTime = Date().time

                Pasteur.debug(TAG, "time spend=" + (endTime - startTime).toString())

                outputStream.flush()

                fileToSave
            } catch (e0: InterruptedIOException) {
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            // Catch any other exceptions, normally we don't expect this happened.
            ToastService.sendShortToast(e.message)
            null
        }
    }

    /**
     * Get file to save given a [expectedName].
     */
    fun getFileToSave(expectedName: String): File? {
        val galleryPath = FileUtil.galleryPath ?: return null
        val folder = File(galleryPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder.toString() + File.separator + expectedName)
    }

    /**
     * Cancel the download of specified [image].
     */
    fun cancelDownload(context: Context, image: UnsplashImage) {
        val intent = Intent(App.instance, DownloadService::class.java)
        intent.putExtra(Params.CANCELED_KEY, true)
        intent.putExtra(Params.URL_KEY, image.downloadUrl)
        context.startService(intent)
    }

    /**
     * Start downloading the [image].
     * @param context used to check network status
     */
    fun download(context: Activity, image: UnsplashImage) {
        if (!PermissionUtil.check(context)) {
            ToastService.sendShortToast(context.getString(R.string.no_permission))
            return
        }
        if (LocalSettingHelper.getBoolean(context,
                context.getString(R.string.preference_key_download_via_metered_network), false)) {
            doDownload(context, image)
            return
        }
        if (!context.usingWifi()) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.attention)
            builder.setMessage(R.string.wifi_attention_content)
            builder.setPositiveButton(R.string.download) { dialog, _ ->
                dialog.dismiss()
                doDownload(context, image)
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        } else {
            doDownload(context, image)
        }
    }

    /**
     * Get the realm object of [DownloadItem] given its [id].
     */
    fun getDownloadItemById(id: String?): DownloadItem? {
        val realm = RealmCache.getInstance()
        realm.beginTransaction()
        val item = realm.where(DownloadItem::class.java)
                .equalTo(DownloadItem.ID_KEY, id)
                .findFirst()
        realm.commitTransaction()
        return item
    }

    private fun doDownload(context: Context, image: UnsplashImage) {
        var previewFile: File? = null
        image.listUrl?.let {
            previewFile = FileUtil.getCachedFile(it)
        }
        DownloadReporter.report(image.downloadLocationLink)
        startDownloadService(context, image.fileNameForDownload, image.downloadUrl!!, previewFile?.path)
        persistDownloadItem(context, image)
        EventBus.getDefault().post(DownloadStartedEvent(image.id))
    }

    private fun persistDownloadItem(context: Context, image: UnsplashImage) {
        val downloadItems = RealmCache.getInstance().where(DownloadItem::class.java)
                .findAllSorted(DownloadItem.POSITION_KEY, Sort.DESCENDING)
        var position = 0
        if (downloadItems.size > 0) {
            position = downloadItems[0].position + 1
        }

        ToastService.sendShortToast(context.getString(R.string.downloading_in_background))

        val item = DownloadItem(image.id!!, image.listUrl!!, image.downloadUrl!!,
                image.fileNameForDownload)
        item.position = position
        item.color = image.themeColor
        RealmCache.getInstance().executeTransaction { realm -> realm.copyToRealmOrUpdate(item) }
    }

    private fun startDownloadService(context: Context, name: String, url: String, previewUrl: String? = null) {
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra(Params.NAME_KEY, name)
        intent.putExtra(Params.URL_KEY, url)
        previewUrl?.let {
            intent.putExtra(Params.PREVIEW_URI, previewUrl)
        }
        context.startService(intent)
    }
}
