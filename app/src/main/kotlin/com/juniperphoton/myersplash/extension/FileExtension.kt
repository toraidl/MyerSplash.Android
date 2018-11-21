package com.juniperphoton.myersplash.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.*
import java.lang.Exception

fun File.getFolderLengthInMb(): Long {
    if (!exists()) return 0
    return listFiles().map(File::length).sum() / 1024 / 1024
}

fun File.getLengthInKb(): Long = length() / 1024 / 1024

fun File.sendScanBroadcast(ctx: Context) {
    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    intent.data = Uri.fromFile(this)
    ctx.sendBroadcast(intent)
}

fun File.copyFile(destF: File): Boolean {
    if (!destF.exists()) {
        try {
            if (!destF.createNewFile()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
    }

    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null

    try {
        try {
            inputStream = FileInputStream(this)
            outputStream = FileOutputStream(destF)

            val fileReader = ByteArray(4096)
            while (true) {
                val read = inputStream.read(fileReader)

                if (read == -1) {
                    break
                }

                outputStream.write(fileReader, 0, read)
            }
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }

    return true
}
