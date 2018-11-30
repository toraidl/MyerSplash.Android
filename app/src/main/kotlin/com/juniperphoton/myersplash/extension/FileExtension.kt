package com.juniperphoton.myersplash.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

fun File.getFolderLengthInMb(): Long {
    if (!exists()) return 0
    return listFiles().map(File::length).sum() / 1024 / 1024
}

fun File.getLengthInKB(): Long = length() / 1024 / 1024

fun File.notifyFileUpdated(ctx: Context) {
    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    intent.data = Uri.fromFile(this)
    ctx.sendBroadcast(intent)
}