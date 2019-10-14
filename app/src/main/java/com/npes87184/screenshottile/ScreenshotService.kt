package com.npes87184.screenshottile

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.app.Activity.RESULT_OK
import android.app.Notification
import android.os.*
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.graphics.Color
import android.os.Build


class ScreenshotService : Service() {
    private val myBinder = MyLocalBinder()
    private var handler: Handler? = null
    private var width = 0
    private var height = 0
    private var screenshotPath: File? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var mediaProjection: MediaProjection? = null
    private var captured = false
    private var receiver: ResultReceiver? = null
    private var mediaProjectionManager: MediaProjectionManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    inner class MyLocalBinder : Binder() {
        fun getService() : ScreenshotService {
            return this@ScreenshotService
        }
    }

    override fun onCreate() {
        super.onCreate()
        val imagesDir = File(applicationContext.filesDir, "images")

        imagesDir.mkdirs()
        screenshotPath = File(imagesDir, "ScreenshotTile.png")
        mediaProjectionManager = getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val resultCode = intent.getIntExtra("code", -1)
        val resultData = intent.getParcelableExtra<Intent>("data")
        receiver = intent.getParcelableExtra("receiver")!!

        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData!!)
        Thread.sleep(150)
        screenshot()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = Notification.Builder(this, createNotificationChannel1())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.running))
                .setAutoCancel(true)

            val notification = builder.build()
            startForeground(1, notification)
        } else {
            val builder = NotificationCompat.Builder(this, "")
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.running))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notification = builder.build()
            startForeground(1, notification)
        }
    }

    private fun createNotificationChannel1(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return ""
        }

        /* Build.VERSION.SDK_INT >= Build.VERSION_CODES.O */
        val strChannelID = "com.npes87184.screensharetile.screenshot"
        val strChannelName = getString(R.string.app_name)
        val chan = NotificationChannel(
            strChannelID,
            strChannelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.GREEN
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(chan)
        return strChannelID
    }

    private fun screenshot() {
        val window = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()

        window.defaultDisplay.getRealMetrics(metrics)
        width = metrics.widthPixels
        height = metrics.heightPixels

        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels,
            PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "Screenshot",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
            imageReader?.surface,
            null,
            handler
        )
        imageReader?.setOnImageAvailableListener(onImageAvailableListener, handler)
        mediaProjection?.registerCallback(MediaProjectionStopCallback(), handler)
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        var image: Image? = null
        var fos: FileOutputStream? = null
        var bitmap: Bitmap? = null

        try {
            image = reader.acquireLatestImage()
            if (image != null && !captured) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width

                bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height, Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                fos = FileOutputStream(screenshotPath)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                mediaProjection?.stop()
                captured = true
                receiver!!.send(RESULT_OK, Bundle())
                mediaProjection?.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            bitmap?.recycle()
            image?.close()
            stopSelf()
        }
    }

    private inner class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            handler?.post {
                if (virtualDisplay != null) {
                    virtualDisplay?.release()
                }
                if (imageReader != null) {
                    imageReader?.setOnImageAvailableListener(null, null)
                }
                mediaProjection?.unregisterCallback(this@MediaProjectionStopCallback)
            }
        }
    }
}