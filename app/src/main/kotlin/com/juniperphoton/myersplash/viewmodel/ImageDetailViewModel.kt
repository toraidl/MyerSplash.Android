package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.db.DetailImageRepo
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.AnalysisHelper
import com.juniperphoton.myersplash.utils.DownloadUtils
import com.juniperphoton.myersplash.utils.FileUtils
import com.juniperphoton.myersplash.utils.Toaster
import com.juniperphoton.myersplash.view.ImageDetailViewContract
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import java.io.File

class ImageDetailViewModel(app: Application) : AndroidViewModel(app), CoroutineScope by MainScope() {
    private val app = getApplication<App>()
    private val repo = DetailImageRepo()

    var viewContract: ImageDetailViewContract? = null
    var unsplashImage: UnsplashImage? = null

    private var prevItem: DownloadItem? = null

    var associatedDownloadItem: Flowable<DownloadItem>? = null
        get() {
            if (field == null) {
                field = repo.retrieveAssociatedItem(unsplashImage?.id ?: "")
            }
            return field?.doOnNext {
                prevItem = it
            }
        }

    fun navigateToAuthorPage() {
        unsplashImage?.userHomePage?.let {
            viewContract?.navigateToAuthorPage(it)
        }
    }

    fun copyUrlToClipboard() {
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(app.getString(R.string.app_name), unsplashImage?.downloadUrl)
        clipboard.primaryClip = clip
    }

    fun share() {
        val image = unsplashImage ?: return
        val file = FileUtils.getCachedFile(image.listUrl!!)

        if (file == null || !file.exists()) {
            Toaster.sendShortToast(app.getString(R.string.something_wrong))
            return
        }

        val shareText = app.getString(R.string.share_text, image.userName, image.downloadUrl)
        val contentUri = FileProvider.getUriForFile(app,
                app.getString(R.string.authorities), file)
        viewContract?.launchShare(contentUri, shareText)
    }

    fun download() {
        AnalysisHelper.logClickDownloadInDetails()
        unsplashImage?.let {
            DownloadUtils.download(app, it)
        }
    }

    fun cancelDownload(): Boolean = runBlocking {
        AnalysisHelper.logClickCancelDownloadInDetails()
        val image = unsplashImage ?: return@runBlocking false

        repo.setStatusById(image.id!!, DownloadItem.DOWNLOAD_STATUS_FAILED)

        DownloadUtils.cancelDownload(app, image)
        return@runBlocking true
    }

    fun setAs() {
        AnalysisHelper.logClickSetAsInDetails()
        val url = "${prevItem?.filePath}"
        viewContract?.launchEditActivity(Uri.fromFile(File(url)))
    }

    fun onHide() {
        associatedDownloadItem = null
        unsplashImage = null
    }
}
