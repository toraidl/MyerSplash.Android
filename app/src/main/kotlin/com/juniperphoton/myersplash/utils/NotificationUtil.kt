package com.juniperphoton.myersplash.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.EditActivity
import com.juniperphoton.myersplash.activity.ManageDownloadActivity
import com.juniperphoton.myersplash.service.DownloadService
import java.io.File

@Suppress("unused")
object NotificationUtil {
    private const val TAG = "NotificationUtil"
    private val NOTIFICATION_CHANNEL_ID = "default_channel"

    private val notificationManager: NotificationManager
        get() {
            return App.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

    init {
        @TargetApi(Build.VERSION_CODES.O)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "App Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT)

            // Configure the notification channel.
            notificationChannel.description = "App notification"
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.importance = NotificationManager.IMPORTANCE_LOW
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun cancelNotification(downloadUri: Uri) {
        notificationManager.cancel(downloadUri.hashCode())
    }

    fun showErrorNotification(downloadUri: Uri, fileName: String, url: String, previewUri: Uri?) {
        val id = downloadUri.hashCode()

        val intent = Intent(App.instance, DownloadService::class.java)
        intent.putExtra(Params.NAME_KEY, fileName)
        intent.putExtra(Params.URL_KEY, url)

        val resultPendingIntent = PendingIntent.getService(App.instance, 0, intent, 0)

        val builder = NotificationCompat.Builder(App.instance, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(App.instance.getString(R.string.download_error))
                .setContentText(App.instance.getString(R.string.download_error_retry))
                .setSmallIcon(R.drawable.vector_ic_clear_white)
        previewUri?.let {
            val bm = BitmapFactory.decodeFile(it.toString())
            builder.setLargeIcon(bm)
        }
        builder.addAction(R.drawable.ic_replay_white, App.instance.getString(R.string.retry_act),
                resultPendingIntent)
        notificationManager.notify(id, builder!!.build())
    }

    fun showCompleteNotification(downloadUri: Uri, previewUri: Uri?, filePath: String?) {
        val id: Int = downloadUri.hashCode()

        val builder = NotificationCompat.Builder(App.instance, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(App.instance.getString(R.string.saved))
                .setContentText(App.instance.getString(R.string.tap_to_open_manage))
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.small_download_ok)
        previewUri?.let {
            val bm = BitmapFactory.decodeFile(it.toString())
            bm?.let {
                builder.setLargeIcon(it)
            }
        }
        if (filePath != null) {
            injectViewIntent(builder, filePath)
        } else {
            injectAppIntent(builder)
        }

        Pasteur.info(TAG, "completed: $downloadUri")
        notificationManager.notify(id, builder.build())
    }

    fun showProgressNotification(title: String, content: String,
                                 progress: Int, downloadUri: Uri, previewUri: Uri?) {
        val id = downloadUri.hashCode()

        val builder = NotificationCompat.Builder(App.instance, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.vector_ic_file_download)
                .setProgress(100, progress, false)
        previewUri?.let {
            val bm = BitmapFactory.decodeFile(it.toString())
            bm?.let {
                builder.setLargeIcon(it)
            }
        }
        injectAppIntent(builder)
        notificationManager.notify(id, builder.build())
    }

    private fun injectAppIntent(builder: NotificationCompat.Builder) {
        val intent = Intent(App.instance, ManageDownloadActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(App.instance, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)
    }

    private fun injectViewIntent(builder: NotificationCompat.Builder, filePath: String) {
        val intent = Intent(App.instance, EditActivity::class.java)
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(filePath)))
        val resultPendingIntent = PendingIntent.getActivity(App.instance, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)
    }
}