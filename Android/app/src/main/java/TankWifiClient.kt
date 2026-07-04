package com.rcdriving.tankrtk

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

class TankWifiClient(
    private val host: String,
    private val port: Int
) {
    @Volatile private var socket: Socket? = null
    @Volatile private var out: OutputStream? = null

    private val _status = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val status: StateFlow<ConnectionStatus> = _status

    fun connect() {
        _status.value = ConnectionStatus.CONNECTING
        thread {
            try {
                val s = Socket()
                s.connect(InetSocketAddress(host, port), 3000)
                socket = s
                out = s.getOutputStream()
                _status.value = ConnectionStatus.CONNECTED
            } catch (_: Exception) {
                _status.value = ConnectionStatus.FAILED
            }
        }
    }

    fun send(cmd: String) {
        try {
            out?.write(cmd.toByteArray())
            out?.flush()
        } catch (_: Exception) {
            _status.value = ConnectionStatus.FAILED
        }
    }

    fun disconnect() {
        try {
            out?.close()
            socket?.close()
        } catch (_: Exception) {}
        out = null
        socket = null
        _status.value = ConnectionStatus.DISCONNECTED
    }
}