package com.juniperphoton.myersplash.utils

import com.juniperphoton.myersplash.cloudservice.CloudService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Report download behavior to Unsplash server.
 */
object DownloadReporter {
    private const val TAG = "DownloadReporter"

    fun report(downloadLocation: String?) {
        val url = downloadLocation ?: return

        GlobalScope.launch {
            CloudService.reportDownload(url)
            Pasteur.info(TAG, "successfully report $url")
        }
    }
}