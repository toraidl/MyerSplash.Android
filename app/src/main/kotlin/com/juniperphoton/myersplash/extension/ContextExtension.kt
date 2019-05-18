package com.juniperphoton.myersplash.extension

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.juniperphoton.myersplash.R

fun Context.getScreenWidth(): Int = resources.displayMetrics.widthPixels

fun Context.getScreenHeight(): Int = resources.displayMetrics.heightPixels

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
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        checkWifiAPI28(manager)
    } else {
        checkWifiAPIPre28(manager)
    }
}

@TargetApi(Build.VERSION_CODES.P)
private fun checkWifiAPI28(manager: ConnectivityManager): Boolean {
    val network = manager.activeNetwork
    val cap = manager.getNetworkCapabilities(network)
    return cap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: return false
}

@Suppress("DEPRECATION")
private fun checkWifiAPIPre28(manager: ConnectivityManager): Boolean {
    val info = manager.activeNetworkInfo
    return info?.type == ConnectivityManager.TYPE_WIFI
}

@Suppress("unused")
fun Context.getVersionName(): String? {
    return try {
        val manager = packageManager
        val info = manager.getPackageInfo(packageName, 0)
        "Version ${info.versionName}"
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

fun Context.startServiceSafely(intent: Intent) {
    try {
        startService(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}