package com.smartico.androidsdk

import android.content.Context
import com.smartico.androidsdk.model.request.InitSession
import com.smartico.androidsdk.model.request.UA
import com.smartico.androidsdk.network.WebSocketConnector
import java.lang.ref.WeakReference

class SmarticoSdk private constructor() {

    companion object {
        val instance = SmarticoSdk()
        val libraryVersion = "1.0.0"
        val os = "Android"
    }

    private var webSocketConnector: WebSocketConnector? = null
    lateinit var context: WeakReference<Context>

    fun init(context: Context, label: String, brand: String) {
        log("initialize")
        this.context = WeakReference(context)
        webSocketConnector = WebSocketConnector()
        webSocketConnector?.startConnector()

        // TODO: Think of a way to differentiate device types
        val ua = UA(
            fp = null,
            agent = "SmarticoAndroidSDK/$libraryVersion",
            host = null,
            deviceType = "NATIVE_PHONE",
            tzoffset = OSUtils.timezoneOffsetInMins(),
            os = os
        )
        webSocketConnector?.sendMessage(
            InitSession(
                cid = 3,
                labelName = label,
                brandKey = brand,
                deviceId = OSUtils.deviceId(),
                page = null,
                trackerVersion = libraryVersion,
                sessionId = OSUtils.generateNextRandomId(),
                ua = ua
            )
        ) {

        }
    }
}

fun Any.log(msg: String) {
    println("SmartiCoSDK: $msg")
}

fun Any.log(throwable: Throwable) {
    println("SmartiCoSDK: Error")
    throwable.printStackTrace()
}
