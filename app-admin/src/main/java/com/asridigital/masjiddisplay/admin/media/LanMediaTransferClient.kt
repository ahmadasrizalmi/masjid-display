package com.asridigital.masjiddisplay.admin.media

import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.MediaDeleteRequest
import com.asridigital.masjiddisplay.protocol.MediaMutationResponse
import com.asridigital.masjiddisplay.protocol.MediaSessionResponse
import com.asridigital.masjiddisplay.protocol.MediaTransportPaths
import com.asridigital.masjiddisplay.protocol.MediaUploadSessionRequest
import com.asridigital.masjiddisplay.protocol.MediaWireContract
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

interface LocalMediaSource {
    val mediaId: String
    val filename: String
    val mimeType: String
    val byteSize: Long
    val sha256: String
    fun openStream(): InputStream
}

interface MediaTransferClient {
    fun upload(
        device: DiscoveredTvService,
        credentialId: String,
        source: LocalMediaSource,
        onProgress: (sentBytes: Long, totalBytes: Long) -> Unit,
    ): Result<Unit>

    fun delete(device: DiscoveredTvService, credentialId: String, mediaId: String): Result<Unit>
}

/** Direct-LAN media client. Host/port always come from NSD resolution, never fixed configuration. */
class LanMediaTransferClient : MediaTransferClient {
    override fun upload(
        device: DiscoveredTvService,
        credentialId: String,
        source: LocalMediaSource,
        onProgress: (Long, Long) -> Unit,
    ): Result<Unit> = runCatching {
        val sessionRequest = MediaUploadSessionRequest(
            credentialId = credentialId,
            mediaId = source.mediaId,
            filename = source.filename,
            mimeType = source.mimeType,
            byteSize = source.byteSize,
            sha256 = source.sha256,
        )
        val sessionBody = postForm(device, MediaTransportPaths.CREATE_SESSION, MediaWireContract.encodeSessionRequest(sessionRequest))
        val session = MediaWireContract.decodeSessionResponse(sessionBody)
            ?: error("TV mengembalikan response session media yang tidak valid")
        val sessionId = when (session) {
            is MediaSessionResponse.Accepted -> session.sessionId
            is MediaSessionResponse.Rejected -> error("${session.code}: ${session.message}")
        }

        val connection = connection(device, MediaTransportPaths.UPLOAD_PREFIX + sessionId).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Content-Type", MediaWireContract.BINARY_CONTENT_TYPE)
            setFixedLengthStreamingMode(source.byteSize)
        }
        source.openStream().use { input ->
            connection.outputStream.use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var sent = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    output.write(buffer, 0, count)
                    sent += count
                    onProgress(sent, source.byteSize)
                }
                require(sent == source.byteSize) { "Ukuran media berubah saat transfer" }
            }
        }
        val responseBody = connection.readResponseBody()
        val response = MediaWireContract.decodeMutationResponse(responseBody)
            ?: error("TV mengembalikan response upload yang tidak valid")
        if (response is MediaMutationResponse.Rejected) error("${response.code}: ${response.message}")
    }

    override fun delete(device: DiscoveredTvService, credentialId: String, mediaId: String): Result<Unit> = runCatching {
        val body = postForm(
            device,
            MediaTransportPaths.DELETE,
            MediaWireContract.encodeDeleteRequest(MediaDeleteRequest(credentialId, mediaId)),
        )
        when (val response = MediaWireContract.decodeMutationResponse(body)) {
            MediaMutationResponse.Success -> Unit
            is MediaMutationResponse.Rejected -> error("${response.code}: ${response.message}")
            null -> error("TV mengembalikan response hapus media yang tidak valid")
        }
    }

    private fun postForm(device: DiscoveredTvService, path: String, body: String): String {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        val connection = connection(device, path).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Content-Type", MediaWireContract.FORM_CONTENT_TYPE)
            setFixedLengthStreamingMode(bytes.size)
        }
        connection.outputStream.use { it.write(bytes) }
        return connection.readResponseBody()
    }

    private fun connection(device: DiscoveredTvService, path: String): HttpURLConnection =
        (URL("http", device.hostAddress, device.port, path).openConnection() as HttpURLConnection)

    private fun HttpURLConnection.readResponseBody(): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        val body = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (responseCode !in 200..299) {
            MediaWireContract.decodeMutationResponse(body)?.let { response ->
                if (response is MediaMutationResponse.Rejected) error("${response.code}: ${response.message}")
            }
            MediaWireContract.decodeSessionResponse(body)?.let { response ->
                if (response is MediaSessionResponse.Rejected) error("${response.code}: ${response.message}")
            }
            error("HTTP $responseCode dari TV")
        }
        return body
    }

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 5_000
        const val READ_TIMEOUT_MILLIS = 30_000
    }
}
