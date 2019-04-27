package com.juniperphoton.myersplash.view

import android.net.Uri

interface ImageDetailViewContract {
    fun launchEditActivity(uri: Uri)
    fun launchShare(uri: Uri, text: String)
    fun navigateToAuthorPage(url: String)
}