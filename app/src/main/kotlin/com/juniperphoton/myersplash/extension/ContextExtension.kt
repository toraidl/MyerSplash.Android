package com.juniperphoton.myersplash.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.ConnectivityManager
import com.juniperphoton.myersplash.R

fun Context.getDpi(): Float = resources.displayMetrics.density

fun Context.getDimenInPixel(valueInDP: Int): Int = (valueInDP * getDpi()).toInt()

fun Context.getScreenWidth(): Int = resources.displayMetrics.widthPixels

fun Context.getScreenHeight(): Int = resources.displayMetrics.heightPixels

fun Context.hasNavigationBar(): Boolean {
    val size = getNavigationBarSize()
    return size.y > 0
}

fun Context.getStatusBarHeight(): Int {
    return try {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            throw IllegalArgumentException("no res id for status bar height")
        }
    } catch (e: Exception) {
        resources.getDimensionPixelSize(R.dimen.status_bar_height)
    }
}

fun Context.getNavigationBarSize(): Point {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        Point(getScreenWidth(), resources.getDimensionPixelSize(resourceId))
    } else Point(0, 0)
}

fun Context.usingWifi(): Boolean {
    val manager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = manager.activeNetworkInfo
    return info?.type == ConnectivityManager.TYPE_WIFI
}

@Suppress("unused")
fun Context.getVersionCode(): Int {
    return try {
        val manager = packageManager
        val info = manager.getPackageInfo(packageName, 0)
        info.versionCode
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }
}

@Suppress("unused")
fun Context.getVersionName(): String? {
    return try {
        val manager = packageManager
        val info = manager.getPackageInfo(packageName, 0)
        "${info.versionName} Build ${info.versionCode}"
    } catch (e: Exception) {
        null
    }
}

fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}