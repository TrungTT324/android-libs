package hdisoft.app.webserver.websocket

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.disposables.Disposable

/**
 * SignalR counterpart to [WebSocketClient]: same shape (construct with a
 * URL + log/status callbacks, `connect()`/`send()`/`closeBlocking()`/
 * `isOpen`) so callers can swap between a raw websocket and an ASP.NET Core
 * SignalR hub without changing call sites. Unlike [WebSocketClient], this
 * wraps `HubConnection` by composition rather than inheritance — the
 * SignalR Java client exposes no overridable lifecycle hooks to subclass,
 * only callback registration (`on`, `onClosed`).
 *
 * [hubMethodName] is the single hub method used both to register the
 * incoming-message handler and to send outgoing messages, matching the
 * classic SignalR chat-hub sample convention (defaults to "Send").
 */
class SignalRClient(
    hubUrl: String,
    private val onLogReceived: (String) -> Unit,
    private val onStatusText: (String) -> Unit,
    private val getStatus: () -> String,
    private val hubMethodName: String = "Send",
) {
    private val hubConnection: HubConnection = HubConnectionBuilder.create(hubUrl).build()
    private var startDisposable: Disposable? = null

    init {
        hubConnection.on(hubMethodName, { message: String -> onLogReceived(message) }, String::class.java)
        hubConnection.onClosed { onStatusText(getStatus()) }
    }

    val isOpen: Boolean
        get() = hubConnection.connectionState == HubConnectionState.CONNECTED

    fun connect() {
        startDisposable = hubConnection.start().subscribe(
            { onStatusText(getStatus()) },
            { ex -> onStatusText("Connection Error: ${ex.message}") },
        )
    }

    fun send(message: String) {
        if (isOpen) {
            hubConnection.send(hubMethodName, message)
        }
    }

    fun closeBlocking() {
        startDisposable?.dispose()
        try {
            hubConnection.stop().blockingAwait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
