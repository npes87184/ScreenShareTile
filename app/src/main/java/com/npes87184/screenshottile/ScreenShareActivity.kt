package com.npes87184.screenshottile

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.npes87184.screenshottile.utils.Define
import com.npes87184.screenshottile.utils.ScreenshotResultReceiver
import iamutkarshtiwari.github.io.ananas.editimage.EditImageActivity
import iamutkarshtiwari.github.io.ananas.editimage.ImageEditorIntentBuilder
import java.io.File


class ScreenShareActivity : AppCompatActivity(), ScreenshotResultReceiver.Receiver {
    private val requestMediaProject = 5566
    private lateinit var screenshotPath: File
    private val receiver: ScreenshotResultReceiver = ScreenshotResultReceiver(Handler())
    private lateinit var imageEditorLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagesDir = File(applicationContext.filesDir, "images")
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        imagesDir.mkdirs()
        screenshotPath = File(imagesDir, "ScreenshotTile.jpg")
        receiver.setReceiver(this)
        imageEditorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                sendScreenshot()
                finish()
            }
        }
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
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (resultCode == RESULT_OK) {
            startEditScreenshot()
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

    private fun startEditScreenshot() {
        try {
            val intent = ImageEditorIntentBuilder(this, screenshotPath.absolutePath, screenshotPath.absolutePath)
                .withPaintFeature()
                .withRotateFeature()
                .withCropFeature()
                .build();
            EditImageActivity.start(imageEditorLauncher, intent, this)
        } catch (e: Exception) {
        }
    }

    private fun sendScreenshot() {
        val authority = "${BuildConfig.APPLICATION_ID}.fileprovider"
        val uri = FileProvider.getUriForFile(applicationContext, authority, screenshotPath)
        val shareIntent = Intent()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val sharingMsgDef = getString(R.string.setting_sharing_msg_default)
        val sharingMsg = prefs.getString(Define.SHARING_MSG, sharingMsgDef) ?: sharingMsgDef

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setDataAndType(uri, applicationContext.contentResolver.getType(uri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(Intent.EXTRA_TEXT, sharingMsg)
        startActivity(Intent.createChooser(shareIntent, "Share screenshot to:"))
    }
}
