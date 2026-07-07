package com.rcdriving.tankrtk

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class TankWifiClient(
    private val host: String,
    private val port: Int
) {
    @Volatile private var socket: Socket? = null
    @Volatile private var out: OutputStream? = null
    @Volatile private var writerThread: Thread? = null

    private val _status = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val status: StateFlow<ConnectionStatus> = _status

    // Holds only the single most recent outgoing command. This is a
    // real-time control channel (joystick position), so a stale queued
    // command is worse than useless — always send the latest one.
    private val outbox = ArrayBlockingQueue<String>(1)

    // Bumped on every connect()/disconnect() call. Lets an in-flight
    // retry loop notice it's been superseded (e.g. the user tapped
    // Disconnect mid-retry) and quietly stop instead of clobbering
    // status/socket state after the fact.
    @Volatile private var connectEpoch = 0

    // The first TCP attempt right after joining TankAP (via
    // WifiNetworkSpecifier) can fail even though the WiFi join itself
    // succeeded — there's a brief window where the phone's routing to
    // the new network hasn't fully settled yet. Retrying a few times
    // with a short pause covers that window instead of requiring the
    // user to keep tapping Connect manually.
    private val maxConnectAttempts = 4
    private val retryDelayMs = 800L

    fun connect() {
        val epoch = ++connectEpoch
        _status.value = ConnectionStatus.CONNECTING
        thread {
            for (attempt in 1..maxConnectAttempts) {
                if (epoch != connectEpoch) return@thread   // superseded

                try {
                    val s = Socket()
                    s.connect(InetSocketAddress(host, port), 3000)

                    if (epoch != connectEpoch) {
                        s.close()
                        return@thread
                    }

                    socket = s
                    out = s.getOutputStream()
                    _status.value = ConnectionStatus.CONNECTED
                    startWriter()
                    return@thread
                } catch (_: Exception) {
                    if (attempt < maxConnectAttempts) {
                        Thread.sleep(retryDelayMs)
                    } else if (epoch == connectEpoch) {
                        _status.value = ConnectionStatus.FAILED
                    }
                }
            }
        }
    }

    // Safe to call from ANY thread, including the UI thread (e.g. the
    // joystick's onMove callback). This used to write to the socket
    // directly and synchronously, which threw NetworkOnMainThreadException
    // when called from the UI thread — silently caught and reported as a
    // disconnect on every single joystick move. Now it just hands the
    // command to a background writer thread via a queue.
    fun send(cmd: String) {
        outbox.poll()      // drop any not-yet-sent stale command
        outbox.offer(cmd)
    }

    private fun startWriter() {
        writerThread = thread {
            try {
                while (socket?.isConnected == true && !Thread.currentThread().isInterrupted) {
                    val cmd = outbox.poll(500, TimeUnit.MILLISECONDS) ?: continue
                    out?.write(cmd.toByteArray())
                    out?.flush()
                }
            } catch (_: Exception) {
                _status.value = ConnectionStatus.FAILED
            }
        }
    }

    fun disconnect() {
        connectEpoch++   // invalidate any in-flight connect() retry loop
        writerThread?.interrupt()
        writerThread = null
        try {
            out?.close()
            socket?.close()
        } catch (_: Exception) {}
        out = null
        socket = null
        outbox.clear()
        _status.value = ConnectionStatus.DISCONNECTED
    }
}
