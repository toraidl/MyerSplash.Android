package com.juniperphoton.myersplash.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.broadcastreceiver.WallpaperWidgetProvider

object AppWidgetUtils {
    fun doWithWidgetId(block: ((Int) -> Unit)) {
        val context = App.instance
        val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, WallpaperWidgetProvider::class.java))
        ids.forEach {
            block.invoke(it)
        }
    }
}