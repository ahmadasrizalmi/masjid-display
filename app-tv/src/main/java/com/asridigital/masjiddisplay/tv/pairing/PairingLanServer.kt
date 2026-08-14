package com.asridigital.masjiddisplay.tv.pairing

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * TV-local HTTP boundary for pairing only. The operating system selects a free LAN port; callers
 * advertise [port] through NSD. This server exposes no cloud or global endpoints.
 */
class PairingLanServer(
    private val handler: PairingHttpHandler,
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
        val request = socket.getInputStream().buffered().readRequest()
        val response = request?.let(handler::handle)
            ?: PairingHttpResponse(400, "text/plain; charset=utf-8", "")
        BufferedOutputStream(socket.getOutputStream()).use { output ->
            val bytes = response.body.toByteArray(StandardCharsets.UTF_8)
            val reason = when (response.status) {
                200 -> "OK"
                400 -> "Bad Request"
                404 -> "Not Found"
                405 -> "Method Not Allowed"
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

    private fun BufferedInputStream.readRequest(): PairingHttpRequest? {
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
        if (contentLength !in 0..MAX_BODY_BYTES) return null
        val body = ByteArray(contentLength)
        var offset = 0
        while (offset < body.size) {
            val read = read(body, offset, body.size - offset)
            if (read < 0) return null
            offset += read
        }
        return PairingHttpRequest(pieces[0], pieces[1], body.toString(StandardCharsets.UTF_8))
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
        const val MAX_BODY_BYTES = 8_192
    }
}
