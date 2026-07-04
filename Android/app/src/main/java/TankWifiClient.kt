package com.rcdriving.tankrtk

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

    fun connect() {
        thread {
            try {
                val s = Socket()
                s.connect(InetSocketAddress(host, port), 3000)
                socket = s
                out = s.getOutputStream()
            } catch (_: Exception) {}
        }
    }

    fun send(cmd: String) {
        try {
            out?.write(cmd.toByteArray())
            out?.flush()
        } catch (_: Exception) {}
    }

    fun disconnect() {
        try {
            out?.close()
            socket?.close()
        } catch (_: Exception) {}
        out = null
        socket = null
    }
}
