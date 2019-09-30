package com.npes87184.screenshottile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.hardware.display.VirtualDisplay
import java.io.File
import java.io.IOException
import android.view.WindowManager
import androidx.core.content.FileProvider


class ScreenshotActivity : Activity() {
    private val requestMediaProject = 5566
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var handler: Handler? = null
    private var width = 0
    private var height = 0
    private var screenshotPath: File? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var mediaProjection: MediaProjection? = null
    private var captured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagesDir = File(applicationContext.filesDir, "images")

        imagesDir.mkdirs()
        screenshotPath = File(imagesDir, "ScreenshotTile.png")
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        Thread.sleep(200)
        startActivityForResult(mediaProjectionManager?.createScreenCaptureIntent(), requestMediaProject)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestMediaProject != requestCode) {
            finish()
            return
        }
        if (RESULT_OK == resultCode) {
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
            screenShot()
        }
        finish()
    }

    private fun screenShot() {
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

    private fun sendScreenshot() {
        val authority = "${BuildConfig.APPLICATION_ID}.fileprovider"
        val uri = FileProvider.getUriForFile(applicationContext, authority, screenshotPath!!)
        val shareIntent = Intent()

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setDataAndType(uri, applicationContext.contentResolver.getType(uri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(Intent.EXTRA_TEXT, "I'm sharing this screenshot!")
        startActivity(Intent.createChooser(shareIntent, "Share screenshot to:"))
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
                sendScreenshot()
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
