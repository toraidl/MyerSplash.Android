package com.juniperphoton.myersplash.utils

import android.content.Context
import android.content.Intent
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.db.AppDatabase
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.service.DownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

@Suppress("unused_parameter")
object DownloadUtils {
    private const val TAG = "DownloadUtils"

    /**
     * Get file to save given a [expectedName].
     */
    fun getFileToSave(expectedName: String): File? {
        val galleryPath = FileUtils.downloadOutputDir ?: return null
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
    fun download(context: Context, image: UnsplashImage) {
        var previewFile: File? = null
        image.listUrl?.let {
            previewFile = FileUtils.getCachedFile(it)
        }
        DownloadReporter.report(image.downloadLocationLink)
        startDownloadService(context, image.fileNameForDownload, image.downloadUrl!!, previewFile?.path)
        persistDownloadItem(context, image)
        Toaster.sendShortToast(context.getString(R.string.downloading_in_background))
    }

    private fun persistDownloadItem(context: Context, image: UnsplashImage) {
        GlobalScope.launch(Dispatchers.IO) {
            val item = DownloadItem(image.id!!, image.listUrl!!, image.downloadUrl!!,
                    image.fileNameForDownload).apply {
                color = image.themeColor
                width = image.width
                height = image.height
            }
            item.color = image.themeColor
            AppDatabase.instance.downloadItemDao().insertAll(item)
        }
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
