package com.smartico.androidsdk.network

import com.google.gson.Gson
import com.smartico.androidsdk.SmarticoSdk
import com.smartico.androidsdk.log
import com.smartico.androidsdk.model.request.SmarticoWebSocketMessage
import com.smartico.androidsdk.model.response.InitSessionResponse
import com.smartico.androidsdk.model.response.SmarticoWebSocketResponse
import okhttp3.*
import okio.ByteString


class WebSocketConnector: WebSocketListener() {
    private val normalCloseStatus = 1000
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isOpen = false
    private var messages: ArrayList<SmarticoWebSocketMessage> = ArrayList()
    private var callbacks: ArrayList<((SmarticoWebSocketResponse) -> Unit)> = ArrayList()
    private var pendingCallback: ((SmarticoWebSocketResponse) -> Unit)? = null
    private val gson = Gson()

    fun startConnector() {
        val request: Request = Request.Builder().url(serviceUrl()).build()
        webSocket = client.newWebSocket(request, this)
    }

    private fun serviceUrl(): String {
        // TODO: What to put as domain?
        val domain = "androidsmarticosdk.com"
        val codeVersion = SmarticoSdk.libraryVersion
        return "wss://api.smartico.ai/websocket/services?master&domain=$domain&version=$codeVersion"
    }

    fun stopConnector() {
        client.dispatcher.executorService.shutdown()
    }

    fun sendMessage(msg: SmarticoWebSocketMessage, callback:((SmarticoWebSocketResponse) -> Unit)) {
        messages.add(msg)
        callbacks.add(callback)
        checkQueue()
    }

    private fun checkQueue() {
        if(!isOpen || messages.isEmpty()) {
            log("checkQueue open=$isOpen count=${messages.count()}")
            return
        }
        val msg = messages.removeFirst()
        pendingCallback = callbacks.removeFirst()
        val json = gson.toJson(msg)
        webSocket?.send(json)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        isOpen = true
        checkQueue()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        log("Receiving: $text")
        pendingCallback?.let {
            it(InitSessionResponse(test = ""))
            pendingCallback = null
        }
        checkQueue()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        log("Receiving: " + bytes.hex());
        checkQueue()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(normalCloseStatus, null)
        log("Closing: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        log(t)
    }
}