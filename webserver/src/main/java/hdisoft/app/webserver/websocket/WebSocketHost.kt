package hdisoft.app.webserver.websocket

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

/**
 * Generic websocket "host" role: accepts connections and broadcasts every
 * message sent via [broadcastLog] to all of them. Named "Host" (not
 * "Server") to pair with [WebSocketClient] without colliding with
 * java-websocket's own `WebSocketServer`/`WebSocketClient` base class names
 * in the same package. Lives in `:libs:webserver` (not `:libs:logcat`,
 * despite the "log" wording in the connect banner) so any module needing a
 * websocket host/client pair can reuse it without depending on `:libs:logcat`;
 * `:libs:logcat`'s `WebSocketDataSource` is currently its only consumer.
 */
class WebSocketHost(
    port: Int,
    private val onLogReceived: (String) -> Unit,
    private val onStatusText: (String) -> Unit,
    private val getStatus: () -> String,
    // Optional: fires with the raw connection right as it opens, before any message
    // arrives - callers that need the connecting peer's remote address (there's no way
    // to get it from onLogReceived, which only forwards the message text) hook in here.
    // Default keeps this source-compatible with existing 4-arg call sites.
    private val onConnectionOpened: ((WebSocket) -> Unit)? = null,
) : WebSocketServer(InetSocketAddress(port)) {

    private val connectionsList = mutableSetOf<WebSocket>()

    fun getConnectionsCount(): Int = synchronized(connectionsList) { connectionsList.size }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        synchronized(connectionsList) {
            connectionsList.add(conn)
        }
        conn.send("[SYSTEM]: Connected to CI-Deploy Logcat Server")
        onStatusText(getStatus())
        onConnectionOpened?.invoke(conn)
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        synchronized(connectionsList) {
            connectionsList.remove(conn)
        }
        onStatusText(getStatus())
    }

    override fun onMessage(conn: WebSocket, message: String) {
        onLogReceived(message)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        onStatusText("Server Error: ${ex.message}")
    }

    override fun onStart() {
        onStatusText(getStatus())
    }

    fun broadcastLog(log: String) {
        synchronized(connectionsList) {
            for (conn in connectionsList) {
                if (conn.isOpen) {
                    conn.send(log)
                }
            }
        }
    }
}
