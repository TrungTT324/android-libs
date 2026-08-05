package hdisoft.app.webserver.websocket

import org.java_websocket.client.WebSocketClient as JavaWebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * Generic websocket "client" role: connects to a remote [WebSocketHost] and
 * forwards every received message. Lives alongside [WebSocketHost] in
 * `:libs:webserver` so any module can reuse the pair; `:libs:logcat`'s
 * `WebSocketDataSource` and `:app`'s LAN auto-connect startup task are its
 * consumers. The java-websocket base class of the same name is imported
 * under an alias to avoid the collision.
 *
 * [onOpened] runs (with `this` as receiver, so it can call [send] directly)
 * right after the handshake completes — what to send on connect is
 * caller-specific (e.g. a log-producer registration message vs. a plain
 * greeting), so it's not hardcoded here.
 *
 * [onFailure] fires for both a hard connect error and a close that happened before
 * [onOpened] ever ran (e.g. nothing listening on the target port) - added because
 * [onStatusText] alone gives callers no reliable way to distinguish "connection never
 * opened" from any other status text without string-matching; default no-op keeps this
 * source-compatible with existing call sites that only cared about [onStatusText].
 */
class WebSocketClient(
    serverUri: URI,
    private val onLogReceived: (String) -> Unit,
    private val onStatusText: (String) -> Unit,
    private val getStatus: () -> String,
    private val onOpened: WebSocketClient.() -> Unit = {},
    private val onFailure: (String) -> Unit = {},
) : JavaWebSocketClient(serverUri) {

    private var opened = false

    override fun onOpen(handshakedata: ServerHandshake) {
        opened = true
        onStatusText(getStatus())
        onOpened()
    }

    override fun onMessage(message: String) {
        onLogReceived(message)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        onStatusText(getStatus())
        if (!opened) {
            onFailure(reason.ifBlank { "Connection closed (code $code)" })
        }
    }

    override fun onError(ex: Exception) {
        onStatusText("Connection Error: ${ex.message}")
        onFailure(ex.message ?: "Connection error")
    }
}
