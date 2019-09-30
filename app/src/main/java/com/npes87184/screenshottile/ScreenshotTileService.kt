package com.npes87184.screenshottile

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.service.quicksettings.TileService

class ScreenshotTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, ScreenshotActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivityAndCollapse(intent)
    }
}