package com.npes87184.screenshottile

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.npes87184.screenshottile.utils.ScreenshotResultReceiver
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File


class ScreenShareActivity : Activity(), ScreenshotResultReceiver.Receiver {
    private val requestMediaProject = 5566
    private lateinit var screenshotPath: File
    private val receiver: ScreenshotResultReceiver = ScreenshotResultReceiver(Handler())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagesDir = File(applicationContext.filesDir, "images")
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        imagesDir.mkdirs()
        screenshotPath = File(imagesDir, "ScreenshotTile.png")
        receiver.setReceiver(this)
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), requestMediaProject)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestMediaProject == requestCode) {
            if (RESULT_OK == resultCode) {
                startScreenshot(resultCode, data)
            } else {
                Toast.makeText(applicationContext,
                    applicationContext.getString(R.string.screen_captured_permission_missing),
                    Toast.LENGTH_LONG).show()
                finish()
            }
        } else if (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                val cropped = resultUri.toFile()
                cropped.renameTo(screenshotPath)
                sendScreenshot()
            }
            finish()
        }
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (resultCode == RESULT_OK) {
            startCropScreenshot()
        }
    }

    private fun startScreenshot(resultCode: Int, data: Intent?) {
        val service = Intent(this, ScreenshotService::class.java)

        service.putExtra("code", resultCode)
        service.putExtra("data", data)
        service.putExtra("receiver", receiver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service)
        } else {
            startService(service)
        }
    }

    private fun startCropScreenshot() {
        val authority = "${BuildConfig.APPLICATION_ID}.fileprovider"
        val imageUri = FileProvider.getUriForFile(applicationContext, authority, screenshotPath)

        CropImage.activity(imageUri)
            .setInitialCropWindowPaddingRatio(0.toFloat())
            .setActivityTitle(getString(R.string.app_name))
            .setCropMenuCropButtonIcon(R.drawable.baseline_share_white_48)
            .setCropMenuCropButtonTitle(getString(R.string.share))
            .start(this)
    }

    private fun sendScreenshot() {
        val authority = "${BuildConfig.APPLICATION_ID}.fileprovider"
        val uri = FileProvider.getUriForFile(applicationContext, authority, screenshotPath)
        val shareIntent = Intent()

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setDataAndType(uri, applicationContext.contentResolver.getType(uri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(Intent.EXTRA_TEXT, "I'm sharing this screenshot!")
        startActivity(Intent.createChooser(shareIntent, "Share screenshot to:"))
    }
}
