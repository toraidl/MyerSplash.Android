package com.juniperphoton.myersplash.utils

import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import com.juniperphoton.myersplash.App

object IntentUtils {
    fun getSetAsWallpaperIntent(uri: Uri): Intent {
        Pasteur.info("IntentUtils", "getSetAsWallpaperIntent: $uri")
        val intent = WallpaperManager.getInstance(App.instance).getCropAndSetWallpaperIntent(uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}