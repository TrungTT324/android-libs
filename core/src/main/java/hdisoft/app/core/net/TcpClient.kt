package hdisoft.app.core.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Minimal line-based TCP client. Connects to a fixed host:port, reads newline-delimited
 * text, and sends outbound messages via an asynchronous background channel.
 */
class TcpClient {
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val writeLock = Any()
    private val sendChannel = Channel<String>(Channel.UNLIMITED)

    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onLineReceived: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    val isConnected: Boolean
        get() = synchronized(writeLock) {
            socket?.let { it.isConnected && !it.isClosed } == true
        }

    fun connect(
        ip: String,
        port: Int,
        connectTimeoutMs: Int = 5000,
        retryDelayMs: Long = 3000,
        autoReconnect: Boolean = true
    ) {
        stop()
        job = scope.launch {
            do {
                try {
                    val s = Socket()
                    s.connect(InetSocketAddress(ip, port), connectTimeoutMs)
                    s.tcpNoDelay = true
                    s.keepAlive = true

                    synchronized(writeLock) {
                        socket = s
                        outputStream = s.getOutputStream()
                    }

                    onConnected?.invoke()

                    // Asynchronous background writer loop
                    val writerJob = launch {
                        for (msg in sendChannel) {
                            try {
                                val bytes = msg.toByteArray(Charsets.UTF_8)
                                var os: OutputStream? = null
                                synchronized(writeLock) {
                                    os = outputStream
                                }
                                if (os == null) break
                                os?.write(bytes)
                                os?.flush()
                            } catch (e: Exception) {
                                break
                            }
                        }
                    }

                    val reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))
                    var line: String? = null
                    while (isActive && reader.readLine().also { line = it } != null) {
                        line?.let { onLineReceived?.invoke(it) }
                    }

                    writerJob.cancel()
                } catch (e: Exception) {
                    onError?.invoke(e.message ?: "TCP connection error")
                } finally {
                    synchronized(writeLock) {
                        try { socket?.close() } catch (e: Exception) {}
                        socket = null
                        outputStream = null
                    }
                    onDisconnected?.invoke()
                }
                if (autoReconnect && isActive) delay(retryDelayMs)
            } while (autoReconnect && isActive)
        }
    }

    /** Asynchronously queues [message] to be sent on the I/O thread. */
    fun send(message: String): Boolean {
        if (!isConnected) return false
        return sendChannel.trySend(message).isSuccess
    }

    fun stop() {
        job?.cancel()
        job = null
        synchronized(writeLock) {
            try { socket?.close() } catch (e: Exception) {}
            socket = null
            outputStream = null
        }
    }
}
