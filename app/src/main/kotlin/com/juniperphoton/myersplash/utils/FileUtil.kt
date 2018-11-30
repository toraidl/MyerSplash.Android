package com.juniperphoton.myersplash.utils

import android.net.Uri
import android.os.Environment
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.myersplash.App
import java.io.File

object FileUtil {
    val galleryPath: String?
        get() {
            val mediaStorageDir: File = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) ?: return ""
                File(path, "MyerSplash")
            } else {
                val extStorageDirectory = App.instance.filesDir.absolutePath
                File(extStorageDirectory, "MyerSplash")
            }

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }

            return mediaStorageDir.absolutePath
        }

    val cachedPath: String?
        get() = App.instance.cacheDir.absolutePath

    fun getCachedFile(url: String): File? {
        val cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(url)), null)

        var localFile: File? = null

        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
                val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
                localFile = (resource as FileBinaryResource).file
            }
        }

        return localFile
    }
}