package com.mahiinfo.hinduwallpaper.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : Service() {
    companion object {
        const val EXTRA_URL = "download_url"
        const val EXTRA_FILENAME = "filename"
        const val EXTRA_TYPE = "type"   // image | video
        const val CHANNEL_ID = "download_channel"
        const val NOTIF_ID = 1001

        fun start(context: Context, url: String, filename: String, type: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_FILENAME, filename)
                putExtra(EXTRA_TYPE, type)
            }
            context.startForegroundService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val filename = intent.getStringExtra(EXTRA_FILENAME) ?: "download_${System.currentTimeMillis()}"
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "image"

        startForeground(NOTIF_ID, buildNotification("Downloading...", 0))

        Thread {
            downloadFile(url, filename, type)
            stopSelf(startId)
        }.start()

        return START_NOT_STICKY
    }

    private fun downloadFile(urlStr: String, filename: String, type: String) {
        try {
            val dir = if (type == "video")
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).resolve("HinduStatus")
            else
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).resolve("HinduWallpaper")

            dir.mkdirs()
            val file = File(dir, filename)

            val connection = URL(urlStr).openConnection() as HttpURLConnection
            connection.connect()
            val total = connection.contentLengthLong
            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(file)
            val buffer = ByteArray(8192)
            var downloaded = 0L
            var count: Int

            while (input.read(buffer).also { count = it } != -1) {
                output.write(buffer, 0, count)
                downloaded += count
                val progress = if (total > 0) ((downloaded * 100) / total).toInt() else -1
                updateNotification(progress)
            }

            output.flush()
            output.close()
            input.close()

            // Notify media scanner
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = android.net.Uri.fromFile(file)
            })

            notifyComplete(file.absolutePath)
        } catch (e: Exception) {
            Log.e("DownloadService", "Download failed", e)
            notifyError()
        }
    }

    private fun buildNotification(text: String, progress: Int): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hindu Wallpaper")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)

        if (progress in 0..100)
            builder.setProgress(100, progress, progress < 0)

        return builder.build()
    }

    private fun updateNotification(progress: Int) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification("Downloading $progress%", progress))
    }

    private fun notifyComplete(path: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID + 1,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download Complete")
                .setContentText("Saved to gallery")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .build()
        )
    }

    private fun notifyError() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download Failed")
                .setContentText("Please try again")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setAutoCancel(true)
                .build()
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }
}
