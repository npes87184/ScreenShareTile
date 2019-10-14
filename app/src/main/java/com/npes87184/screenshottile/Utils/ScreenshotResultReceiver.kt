package com.npes87184.screenshottile.Utils

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class ScreenshotResultReceiver(handler: Handler)// TODO Auto-generated constructor stub
    : ResultReceiver(handler) {

    private var mReceiver: Receiver? = null

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle)

    }

    fun setReceiver(receiver: Receiver) {
        mReceiver = receiver
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (mReceiver != null) {
            mReceiver!!.onReceiveResult(resultCode, resultData)
        }
    }
}