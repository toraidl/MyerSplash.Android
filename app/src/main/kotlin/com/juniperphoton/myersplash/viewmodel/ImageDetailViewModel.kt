package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.db.DetailImageRepo
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.AnalysisHelper
import com.juniperphoton.myersplash.utils.DownloadUtil
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.Toaster
import com.juniperphoton.myersplash.view.ImageDetailViewContract
import kotlinx.coroutines.*
import java.io.File

class ImageDetailViewModel(app: Application) : AndroidViewModel(app), CoroutineScope by MainScope() {
    private val app = getApplication<App>()
    private val repo = DetailImageRepo()

    var viewContract: ImageDetailViewContract? = null
    var unsplashImage: UnsplashImage? = null

    val associatedDownloadItem: LiveData<DownloadItem>
        get() = repo.retrieveAssociatedItem(unsplashImage?.id ?: "")

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
        val file = FileUtil.getCachedFile(image.listUrl!!)

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
            DownloadUtil.download(app, it)
        }
    }

    fun cancelDownload(): Boolean = runBlocking {
        AnalysisHelper.logClickCancelDownloadInDetails()
        val image = unsplashImage ?: return@runBlocking false

        withContext(Dispatchers.IO) {
            repo.setStatusById(image.id!!, DownloadItem.DOWNLOAD_STATUS_FAILED)
        }

        DownloadUtil.cancelDownload(app, image)
        return@runBlocking true
    }

    fun setAs() {
        val item = associatedDownloadItem
        AnalysisHelper.logClickSetAsInDetails()
        val url = "${item.value?.filePath}"
        viewContract?.launchEditActivity(Uri.fromFile(File(url)))
    }

    fun onHide(lifecycleOwner: LifecycleOwner) {
        associatedDownloadItem.removeObservers(lifecycleOwner)
        unsplashImage = null
    }
}
