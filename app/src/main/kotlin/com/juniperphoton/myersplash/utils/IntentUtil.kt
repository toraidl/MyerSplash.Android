package com.juniperphoton.myersplash.utils

import android.app.WallpaperManager
import android.content.Intent
import androidx.core.content.FileProvider
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import java.io.File

object IntentUtil {
    fun getSetAsWallpaperIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(App.instance,
                App.instance.getString(R.string.authorities), file)
        val intent = WallpaperManager.getInstance(App.instance).getCropAndSetWallpaperIntent(uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}