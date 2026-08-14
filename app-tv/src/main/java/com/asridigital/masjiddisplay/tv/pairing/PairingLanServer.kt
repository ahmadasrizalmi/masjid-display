package com.asridigital.masjiddisplay.tv.pairing

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * TV-local HTTP boundary. Small control messages are buffered; registered media routes receive
 * the socket body as a stream so photo bytes are never converted to String/base64.
 */
class PairingLanServer(
    private val handler: PairingHttpHandler,
    private val streamHandler: ((String, String, Long, InputStream) -> PairingHttpResponse?)? = null,
    private val serverSocketFactory: () -> ServerSocket = { ServerSocket(0) },
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
) : Closeable {
    private var server: ServerSocket? = null
    private var acceptThread: Thread? = null

    @Synchronized
    fun start(): Int {
        server?.let { return it.localPort }
        val socket = serverSocketFactory()
        server = socket
        acceptThread = Thread({ acceptLoop(socket) }, "pairing-lan-server").also {
            it.isDaemon = true
            it.start()
        }
        return socket.localPort
    }

    val port: Int?
        @Synchronized get() = server?.localPort

    private fun acceptLoop(socket: ServerSocket) {
        while (!socket.isClosed) {
            try {
                val client = socket.accept()
                executor.execute { client.use(::handleClient) }
            } catch (_: java.net.SocketException) {
                if (!socket.isClosed) continue
            }
        }
    }

    private fun handleClient(socket: Socket) {
        socket.soTimeout = SOCKET_TIMEOUT_MILLIS
        val input = socket.getInputStream().buffered()
        val head = input.readRequestHead()
        val response = if (head == null) {
            PairingHttpResponse(400, "text/plain; charset=utf-8", "")
        } else {
            val streamed = streamHandler?.invoke(head.method, head.path, head.contentLength.toLong(), input)
            when {
                streamed != null -> streamed
                head.contentLength > MAX_CONTROL_BODY_BYTES -> PairingHttpResponse(413, "text/plain; charset=utf-8", "")
                else -> {
                    val body = input.readExact(head.contentLength)
                    if (body == null) PairingHttpResponse(400, "text/plain; charset=utf-8", "")
                    else handler.handle(PairingHttpRequest(head.method, head.path, body.toString(StandardCharsets.UTF_8)))
                }
            }
        }
        writeResponse(socket, response)
    }

    private fun writeResponse(socket: Socket, response: PairingHttpResponse) {
        BufferedOutputStream(socket.getOutputStream()).use { output ->
            val bytes = response.body.toByteArray(StandardCharsets.UTF_8)
            val reason = when (response.status) {
                200 -> "OK"
                400 -> "Bad Request"
                403 -> "Forbidden"
                404 -> "Not Found"
                405 -> "Method Not Allowed"
                410 -> "Gone"
                413 -> "Payload Too Large"
                507 -> "Insufficient Storage"
                else -> "Internal Server Error"
            }
            output.write("HTTP/1.1 ${response.status} $reason\r\n".toByteArray(StandardCharsets.US_ASCII))
            output.write("Content-Type: ${response.contentType}\r\n".toByteArray(StandardCharsets.US_ASCII))
            output.write("Content-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray(StandardCharsets.US_ASCII))
            output.write(bytes)
            output.flush()
        }
    }

    @Synchronized
    override fun close() {
        server?.close()
        server = null
        acceptThread = null
        executor.shutdownNow()
    }

    private data class RequestHead(val method: String, val path: String, val contentLength: Int)

    private fun BufferedInputStream.readRequestHead(): RequestHead? {
        val requestLine = readAsciiLine() ?: return null
        val pieces = requestLine.split(' ')
        if (pieces.size != 3 || !pieces[2].startsWith("HTTP/")) return null
        var contentLength = 0
        var headerCount = 0
        while (true) {
            val line = readAsciiLine() ?: return null
            if (line.isEmpty()) break
            if (++headerCount > MAX_HEADERS) return null
            val separator = line.indexOf(':')
            if (separator <= 0) return null
            if (line.substring(0, separator).equals("Content-Length", ignoreCase = true)) {
                contentLength = line.substring(separator + 1).trim().toIntOrNull() ?: return null
            }
        }
        if (contentLength !in 0..MAX_STREAM_BODY_BYTES) return null
        return RequestHead(pieces[0], pieces[1], contentLength)
    }

    private fun BufferedInputStream.readExact(size: Int): ByteArray? {
        val body = ByteArray(size)
        var offset = 0
        while (offset < body.size) {
            val read = read(body, offset, body.size - offset)
            if (read < 0) return null
            offset += read
        }
        return body
    }

    private fun BufferedInputStream.readAsciiLine(): String? {
        val bytes = ArrayList<Byte>(MAX_LINE_BYTES)
        while (bytes.size < MAX_LINE_BYTES) {
            val next = read()
            if (next < 0) return null
            if (next == '\n'.code) {
                if (bytes.lastOrNull() == '\r'.code.toByte()) bytes.removeAt(bytes.lastIndex)
                return bytes.toByteArray().toString(StandardCharsets.US_ASCII)
            }
            bytes += next.toByte()
        }
        return null
    }

    private companion object {
        const val SOCKET_TIMEOUT_MILLIS = 5_000
        const val MAX_LINE_BYTES = 2_048
        const val MAX_HEADERS = 32
        const val MAX_CONTROL_BODY_BYTES = 16_384
        const val MAX_STREAM_BODY_BYTES = 50 * 1024 * 1024
    }
}
