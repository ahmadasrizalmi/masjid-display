package com.asridigital.masjiddisplay.tv.pairing

import com.asridigital.masjiddisplay.database.MediaItemDao
import com.asridigital.masjiddisplay.database.MediaItemEntity
import com.asridigital.masjiddisplay.media.AtomicMediaStore
import com.asridigital.masjiddisplay.protocol.MediaListRequest
import com.asridigital.masjiddisplay.protocol.MediaListResponse
import com.asridigital.masjiddisplay.protocol.MediaMutationResponse
import com.asridigital.masjiddisplay.protocol.MediaSessionResponse
import com.asridigital.masjiddisplay.protocol.MediaTransportPaths
import com.asridigital.masjiddisplay.protocol.MediaUploadSessionRequest
import com.asridigital.masjiddisplay.protocol.MediaWireContract
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TvMediaHttpHandlerTest {
    @Test
    fun untrustedCredentialCannotCreateUploadSession() {
        val handler = handler(trusted = false)
        val request = sessionRequest("credential", "media-1", "payload".encodeToByteArray())

        val response = handler.handleControl(
            PairingHttpRequest("POST", MediaTransportPaths.CREATE_SESSION, MediaWireContract.encodeSessionRequest(request)),
        )

        assertEquals(403, response.status)
        assertIs<MediaSessionResponse.Rejected>(MediaWireContract.decodeSessionResponse(response.body))
    }

    @Test
    fun trustedListReadsPersistedRoomMetadata() {
        val dao = FakeMediaDao()
        dao.items.value = listOf(
            MediaItemEntity(
                id = "media-existing",
                localFilename = "media-existing.jpg",
                mediaType = "image/jpeg",
                byteSize = 42,
                checksum = "a".repeat(64),
                width = null,
                height = null,
                createdAtEpochMillis = 123L,
                enabled = true,
            ),
        )
        val handler = handler(dao = dao)

        val response = handler.handleControl(
            PairingHttpRequest(
                "POST",
                MediaTransportPaths.LIST,
                MediaWireContract.encodeListRequest(MediaListRequest("credential")),
            ),
        )

        assertEquals(200, response.status)
        val listed = assertIs<MediaListResponse.Success>(MediaWireContract.decodeListResponse(response.body))
        assertEquals("media-existing", listed.items.single().mediaId)
        assertEquals("media-existing.jpg", listed.items.single().filename)
    }

    @Test
    fun untrustedCredentialCannotListPersistedMedia() {
        val response = handler(trusted = false).handleControl(
            PairingHttpRequest(
                "POST",
                MediaTransportPaths.LIST,
                MediaWireContract.encodeListRequest(MediaListRequest("credential")),
            ),
        )

        assertEquals(403, response.status)
        assertIs<MediaListResponse.Rejected>(MediaWireContract.decodeListResponse(response.body))
    }

    @Test
    fun trustedSessionStreamsVerifiedBytesAndPersistsMetadata() {
        val dao = FakeMediaDao()
        val root = Files.createTempDirectory("tv-media-handler")
        val handler = handler(dao = dao, root = root)
        val bytes = "local-photo".encodeToByteArray()
        val metadata = sessionRequest("credential", "media-1", bytes)
        val create = handler.handleControl(
            PairingHttpRequest("POST", MediaTransportPaths.CREATE_SESSION, MediaWireContract.encodeSessionRequest(metadata)),
        )
        val session = assertIs<MediaSessionResponse.Accepted>(MediaWireContract.decodeSessionResponse(create.body))

        val upload = assertNotNull(
            handler.handleUpload(
                "POST",
                MediaTransportPaths.UPLOAD_PREFIX + session.sessionId,
                bytes.size.toLong(),
                ByteArrayInputStream(bytes),
            ),
        )

        assertEquals(200, upload.status)
        assertEquals(MediaMutationResponse.Success, MediaWireContract.decodeMutationResponse(upload.body))
        assertEquals("media-1", dao.items.value.single().id)
        assertTrue(Files.exists(root.resolve("media/media-1.jpg")))
    }

    @Test
    fun checksumMismatchLeavesNoPersistedMetadata() {
        val dao = FakeMediaDao()
        val root = Files.createTempDirectory("tv-media-handler")
        val handler = handler(dao = dao, root = root)
        val expected = "expected".encodeToByteArray()
        val actual = "tampered".encodeToByteArray()
        val metadata = sessionRequest("credential", "media-2", expected).copy(byteSize = actual.size.toLong())
        val create = handler.handleControl(
            PairingHttpRequest("POST", MediaTransportPaths.CREATE_SESSION, MediaWireContract.encodeSessionRequest(metadata)),
        )
        val session = assertIs<MediaSessionResponse.Accepted>(MediaWireContract.decodeSessionResponse(create.body))

        val upload = assertNotNull(
            handler.handleUpload(
                "POST",
                MediaTransportPaths.UPLOAD_PREFIX + session.sessionId,
                actual.size.toLong(),
                ByteArrayInputStream(actual),
            ),
        )

        val rejected = assertIs<MediaMutationResponse.Rejected>(MediaWireContract.decodeMutationResponse(upload.body))
        assertEquals("CHECKSUM_MISMATCH", rejected.code)
        assertTrue(dao.items.value.isEmpty())
        assertTrue(Files.list(root.resolve(".incoming")).use { it.findAny().isEmpty })
    }

    private fun handler(
        trusted: Boolean = true,
        dao: FakeMediaDao = FakeMediaDao(),
        root: java.nio.file.Path = Files.createTempDirectory("tv-media-handler"),
    ) = TvMediaHttpHandler(
        store = AtomicMediaStore(root),
        mediaDao = dao,
        isCredentialTrusted = { trusted && it == "credential" },
        clock = Clock.fixed(Instant.parse("2026-08-14T00:00:00Z"), ZoneOffset.UTC),
    )

    private fun sessionRequest(credential: String, mediaId: String, bytes: ByteArray) = MediaUploadSessionRequest(
        credentialId = credential,
        mediaId = mediaId,
        filename = "$mediaId.jpg",
        mimeType = "image/jpeg",
        byteSize = bytes.size.toLong(),
        sha256 = sha256(bytes),
    )

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    private class FakeMediaDao : MediaItemDao {
        val items = MutableStateFlow<List<MediaItemEntity>>(emptyList())
        override fun observeAll(): Flow<List<MediaItemEntity>> = items
        override suspend fun getAll(): List<MediaItemEntity> = items.value.sortedByDescending { it.createdAtEpochMillis }
        override suspend fun upsert(item: MediaItemEntity) {
            items.value = items.value.filterNot { it.id == item.id } + item
        }
        override suspend fun deleteById(id: String) {
            items.value = items.value.filterNot { it.id == id }
        }
    }
}
