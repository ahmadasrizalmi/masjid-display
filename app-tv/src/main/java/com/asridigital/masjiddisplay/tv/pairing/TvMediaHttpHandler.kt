package com.asridigital.masjiddisplay.tv.pairing

import com.asridigital.masjiddisplay.database.MediaItemDao
import com.asridigital.masjiddisplay.database.MediaItemEntity
import com.asridigital.masjiddisplay.media.AtomicMediaStore
import com.asridigital.masjiddisplay.media.MediaStoreError
import com.asridigital.masjiddisplay.media.MediaStoreResult
import com.asridigital.masjiddisplay.media.MediaUploadSpec
import com.asridigital.masjiddisplay.protocol.MediaDeleteRequest
import com.asridigital.masjiddisplay.protocol.MediaListItem
import com.asridigital.masjiddisplay.protocol.MediaListResponse
import com.asridigital.masjiddisplay.protocol.MediaMutationResponse
import com.asridigital.masjiddisplay.protocol.MediaSessionResponse
import com.asridigital.masjiddisplay.protocol.MediaTransportPaths
import com.asridigital.masjiddisplay.protocol.MediaUploadSessionRequest
import com.asridigital.masjiddisplay.protocol.MediaWireContract
import java.io.InputStream
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.runBlocking

/** TV-local media transfer boundary. Session creation is small/form encoded; upload bytes are streamed. */
class TvMediaHttpHandler(
    private val store: AtomicMediaStore,
    private val mediaDao: MediaItemDao,
    private val isCredentialTrusted: (String) -> Boolean,
    private val clock: Clock = Clock.systemUTC(),
    private val sessionTtl: Duration = Duration.ofMinutes(10),
) {
    private data class Session(val request: MediaUploadSessionRequest, val expiresAt: Instant)
    private val sessions = ConcurrentHashMap<String, Session>()

    fun handleControl(request: PairingHttpRequest): PairingHttpResponse = when (request.path) {
        MediaTransportPaths.CREATE_SESSION -> createSession(request)
        MediaTransportPaths.LIST -> list(request)
        MediaTransportPaths.DELETE -> delete(request)
        else -> response(404, "")
    }

    fun handleUpload(
        method: String,
        path: String,
        contentLength: Long,
        body: InputStream,
    ): PairingHttpResponse? {
        if (!path.startsWith(MediaTransportPaths.UPLOAD_PREFIX)) return null
        if (method != "POST") return response(405, "")
        val sessionId = path.removePrefix(MediaTransportPaths.UPLOAD_PREFIX)
        if (sessionId.isBlank()) return rejectedMutation(400, "MALFORMED_REQUEST", "Session upload tidak valid")
        val session = sessions.remove(sessionId)
            ?: return rejectedMutation(404, "SESSION_NOT_FOUND", "Session upload tidak ditemukan")
        if (!clock.instant().isBefore(session.expiresAt)) {
            return rejectedMutation(410, "SESSION_EXPIRED", "Session upload sudah kedaluwarsa")
        }
        if (contentLength != session.request.byteSize) {
            return rejectedMutation(400, "SIZE_MISMATCH", "Ukuran stream tidak sesuai metadata")
        }

        val spec = runCatching {
            MediaUploadSpec(
                mediaId = session.request.mediaId,
                originalFilename = session.request.filename,
                mimeType = session.request.mimeType,
                expectedBytes = session.request.byteSize,
                expectedSha256 = session.request.sha256,
            )
        }.getOrElse {
            return rejectedMutation(400, "VALIDATION_FAILED", it.message ?: "Metadata media tidak valid")
        }

        return when (val stored = store.store(spec, body)) {
            is MediaStoreResult.Rejected -> rejectedMutation(
                status = if (stored.reason == MediaStoreError.IO_FAILURE) 507 else 400,
                code = stored.reason.name,
                message = when (stored.reason) {
                    MediaStoreError.SIZE_MISMATCH -> "Ukuran media tidak sesuai"
                    MediaStoreError.CHECKSUM_MISMATCH -> "Checksum media tidak sesuai"
                    MediaStoreError.IO_FAILURE -> "Penyimpanan TV tidak cukup atau gagal ditulis"
                },
            )
            is MediaStoreResult.Success -> {
                try {
                    runBlocking {
                        mediaDao.upsert(
                            MediaItemEntity(
                                id = stored.media.mediaId,
                                localFilename = stored.media.filename,
                                mediaType = session.request.mimeType,
                                byteSize = stored.media.byteSize,
                                checksum = stored.media.sha256,
                                width = null,
                                height = null,
                                createdAtEpochMillis = clock.millis(),
                                enabled = true,
                            ),
                        )
                    }
                    response(200, MediaWireContract.encodeMutationResponse(MediaMutationResponse.Success))
                } catch (_: Exception) {
                    store.delete(stored.media.mediaId)
                    rejectedMutation(500, "PERSIST_FAILED", "Metadata media gagal disimpan")
                }
            }
        }
    }

    private fun createSession(request: PairingHttpRequest): PairingHttpResponse {
        if (request.method != "POST") return response(405, "")
        val metadata = MediaWireContract.decodeSessionRequest(request.body)
            ?: return rejectedSession(400, "MALFORMED_REQUEST", "Metadata upload tidak dapat dibaca")
        if (!isCredentialTrusted(metadata.credentialId)) {
            return rejectedSession(403, "UNAUTHORIZED", "Credential Admin tidak dikenal")
        }
        val valid = runCatching {
            MediaUploadSpec(metadata.mediaId, metadata.filename, metadata.mimeType, metadata.byteSize, metadata.sha256)
        }
        if (valid.isFailure) {
            return rejectedSession(400, "VALIDATION_FAILED", valid.exceptionOrNull()?.message ?: "Metadata media tidak valid")
        }
        purgeExpired()
        val sessionId = UUID.randomUUID().toString()
        sessions[sessionId] = Session(metadata, clock.instant().plus(sessionTtl))
        return response(200, MediaWireContract.encodeSessionResponse(MediaSessionResponse.Accepted(sessionId)))
    }

    private fun list(request: PairingHttpRequest): PairingHttpResponse {
        if (request.method != "POST") return response(405, "")
        val listRequest = MediaWireContract.decodeListRequest(request.body)
            ?: return rejectedList(400, "MALFORMED_REQUEST", "Permintaan daftar media tidak dapat dibaca")
        if (!isCredentialTrusted(listRequest.credentialId)) {
            return rejectedList(403, "UNAUTHORIZED", "Credential Admin tidak dikenal")
        }
        return try {
            val items = runBlocking { mediaDao.getAll() }.map { entity ->
                MediaListItem(
                    mediaId = entity.id,
                    filename = entity.localFilename,
                    mimeType = entity.mediaType,
                    byteSize = entity.byteSize,
                    sha256 = entity.checksum,
                    createdAtEpochMillis = entity.createdAtEpochMillis,
                    enabled = entity.enabled,
                )
            }
            response(200, MediaWireContract.encodeListResponse(MediaListResponse.Success(items)))
        } catch (_: Exception) {
            rejectedList(500, "LIST_FAILED", "TV gagal membaca daftar media")
        }
    }

    private fun delete(request: PairingHttpRequest): PairingHttpResponse {
        if (request.method != "POST") return response(405, "")
        val delete = MediaWireContract.decodeDeleteRequest(request.body)
            ?: return rejectedMutation(400, "MALFORMED_REQUEST", "Permintaan hapus tidak dapat dibaca")
        if (!isCredentialTrusted(delete.credentialId)) {
            return rejectedMutation(403, "UNAUTHORIZED", "Credential Admin tidak dikenal")
        }
        return try {
            runBlocking { mediaDao.deleteById(delete.mediaId) }
            store.delete(delete.mediaId)
            response(200, MediaWireContract.encodeMutationResponse(MediaMutationResponse.Success))
        } catch (_: Exception) {
            rejectedMutation(500, "DELETE_FAILED", "Media gagal dihapus dari TV")
        }
    }

    private fun purgeExpired() {
        val now = clock.instant()
        sessions.entries.removeIf { !now.isBefore(it.value.expiresAt) }
    }

    private fun rejectedSession(status: Int, code: String, message: String) = response(
        status,
        MediaWireContract.encodeSessionResponse(MediaSessionResponse.Rejected(code, message)),
    )

    private fun rejectedList(status: Int, code: String, message: String) = response(
        status,
        MediaWireContract.encodeListResponse(MediaListResponse.Rejected(code, message)),
    )

    private fun rejectedMutation(status: Int, code: String, message: String) = response(
        status,
        MediaWireContract.encodeMutationResponse(MediaMutationResponse.Rejected(code, message)),
    )

    private fun response(status: Int, body: String) = PairingHttpResponse(status, MediaWireContract.FORM_CONTENT_TYPE, body)
}
