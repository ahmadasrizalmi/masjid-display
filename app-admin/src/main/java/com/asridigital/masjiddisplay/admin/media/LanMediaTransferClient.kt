package com.asridigital.masjiddisplay.admin.media

import android.util.Base64
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.MediaDeleteRequest
import com.asridigital.masjiddisplay.protocol.MediaListItem
import com.asridigital.masjiddisplay.protocol.MediaListRequest
import com.asridigital.masjiddisplay.protocol.MediaListResponse
import com.asridigital.masjiddisplay.protocol.MediaMutationResponse
import com.asridigital.masjiddisplay.protocol.MediaSessionResponse
import com.asridigital.masjiddisplay.protocol.MediaThumbnailRequest
import com.asridigital.masjiddisplay.protocol.MediaThumbnailResponse
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
    fun upload(device: DiscoveredTvService, credentialId: String, source: LocalMediaSource, onProgress: (Long, Long) -> Unit): Result<Unit>
    fun delete(device: DiscoveredTvService, credentialId: String, mediaId: String): Result<Unit>
}

/** Bound when ListMedia succeeds, so MediaGrid can request previews from the same NSD-resolved TV. */
internal object MediaThumbnailLoader {
    @Volatile private var load: ((String) -> ByteArray?)? = null
    fun bind(loader: (String) -> ByteArray?) { load = loader }
    fun load(mediaId: String): ByteArray? = load?.invoke(mediaId)
}

class LanMediaTransferClient : MediaTransferClient {
    fun list(device: DiscoveredTvService, credentialId: String): Result<List<MediaListItem>> = runCatching {
        val items = when (val response = MediaWireContract.decodeListResponse(postForm(device, MediaTransportPaths.LIST, MediaWireContract.encodeListRequest(MediaListRequest(credentialId))))) {
            is MediaListResponse.Success -> response.items
            is MediaListResponse.Rejected -> error("${response.code}: ${response.message}")
            null -> error("TV mengembalikan daftar media yang tidak valid")
        }
        MediaThumbnailLoader.bind { mediaId -> thumbnail(device, credentialId, mediaId).getOrNull() }
        items
    }

    fun thumbnail(device: DiscoveredTvService, credentialId: String, mediaId: String): Result<ByteArray> = runCatching {
        val body = postForm(device, MediaTransportPaths.THUMBNAIL, MediaWireContract.encodeThumbnailRequest(MediaThumbnailRequest(credentialId, mediaId)))
        when (val response = MediaWireContract.decodeThumbnailResponse(body)) {
            is MediaThumbnailResponse.Success -> Base64.decode(response.jpegBase64, Base64.DEFAULT)
            is MediaThumbnailResponse.Rejected -> error("${response.code}: ${response.message}")
            null -> error("TV mengembalikan thumbnail yang tidak valid")
        }
    }

    override fun upload(device: DiscoveredTvService, credentialId: String, source: LocalMediaSource, onProgress: (Long, Long) -> Unit): Result<Unit> = runCatching {
        val request = MediaUploadSessionRequest(credentialId, source.mediaId, source.filename, source.mimeType, source.byteSize, source.sha256)
        val session = MediaWireContract.decodeSessionResponse(postForm(device, MediaTransportPaths.CREATE_SESSION, MediaWireContract.encodeSessionRequest(request)))
            ?: error("TV mengembalikan response session media yang tidak valid")
        val sessionId = when (session) {
            is MediaSessionResponse.Accepted -> session.sessionId
            is MediaSessionResponse.Rejected -> error("${session.code}: ${session.message}")
        }
        val connection = connection(device, MediaTransportPaths.UPLOAD_PREFIX + sessionId).apply {
            requestMethod = "POST"; doOutput = true; connectTimeout = CONNECT_TIMEOUT_MILLIS; readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Content-Type", MediaWireContract.BINARY_CONTENT_TYPE); setFixedLengthStreamingMode(source.byteSize)
        }
        try {
            source.openStream().use { input ->
                connection.outputStream.use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE); var sent = 0L
                    while (true) {
                        val count = input.read(buffer); if (count < 0) break; if (count == 0) continue
                        output.write(buffer, 0, count); sent += count; onProgress(sent, source.byteSize)
                    }
                    require(sent == source.byteSize) { "Ukuran media berubah saat transfer" }
                }
            }
            val response = MediaWireContract.decodeMutationResponse(connection.readResponseBody())
                ?: error("TV mengembalikan response upload yang tidak valid")
            if (response is MediaMutationResponse.Rejected) error("${response.code}: ${response.message}")
        } finally { connection.disconnect() }
    }

    override fun delete(device: DiscoveredTvService, credentialId: String, mediaId: String): Result<Unit> = runCatching {
        when (val response = MediaWireContract.decodeMutationResponse(postForm(device, MediaTransportPaths.DELETE, MediaWireContract.encodeDeleteRequest(MediaDeleteRequest(credentialId, mediaId))))) {
            MediaMutationResponse.Success -> Unit
            is MediaMutationResponse.Rejected -> error("${response.code}: ${response.message}")
            null -> error("TV mengembalikan response hapus media yang tidak valid")
        }
    }

    private fun postForm(device: DiscoveredTvService, path: String, body: String): String {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        val connection = connection(device, path).apply {
            requestMethod = "POST"; doOutput = true; connectTimeout = CONNECT_TIMEOUT_MILLIS; readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Content-Type", MediaWireContract.FORM_CONTENT_TYPE); setFixedLengthStreamingMode(bytes.size)
        }
        return try { connection.outputStream.use { it.write(bytes) }; connection.readResponseBody() } finally { connection.disconnect() }
    }

    private fun connection(device: DiscoveredTvService, path: String): HttpURLConnection = URL("http", device.hostAddress, device.port, path).openConnection() as HttpURLConnection

    private fun HttpURLConnection.readResponseBody(): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        val body = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (responseCode !in 200..299) error("HTTP $responseCode dari TV: $body")
        return body
    }

    private companion object { const val CONNECT_TIMEOUT_MILLIS = 5_000; const val READ_TIMEOUT_MILLIS = 30_000 }
}
