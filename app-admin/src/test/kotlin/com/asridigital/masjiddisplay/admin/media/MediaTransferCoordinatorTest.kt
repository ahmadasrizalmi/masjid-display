package com.asridigital.masjiddisplay.admin.media

import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MediaTransferCoordinatorTest {
    @Test
    fun pendingItemsUploadSequentiallyAndSuccessIsNotRepeated() {
        val client = FakeClient()
        val coordinator = MediaTransferCoordinator(client, device(), "credential")
        coordinator.enqueue(listOf(source("a"), source("b")))

        coordinator.uploadPending()
        coordinator.uploadPending()

        assertEquals(listOf("a", "b"), client.uploaded)
        assertIs<MediaTransferState.Success>(coordinator.items[0].state)
        assertIs<MediaTransferState.Success>(coordinator.items[1].state)
    }

    @Test
    fun failedItemRetriesIndividuallyWithoutResendingSuccessfulSibling() {
        val client = FakeClient(failOnce = mutableSetOf("b"))
        val coordinator = MediaTransferCoordinator(client, device(), "credential")
        coordinator.enqueue(listOf(source("a"), source("b")))

        coordinator.uploadPending()
        assertIs<MediaTransferState.Failed>(coordinator.items.first { it.source.mediaId == "b" }.state)
        coordinator.retry("b")

        assertEquals(listOf("a", "b", "b"), client.uploaded)
        assertIs<MediaTransferState.Success>(coordinator.items.first { it.source.mediaId == "a" }.state)
        assertIs<MediaTransferState.Success>(coordinator.items.first { it.source.mediaId == "b" }.state)
    }

    @Test
    fun deleteRemovesSuccessfulItemOnlyAfterTvAcknowledges() {
        val client = FakeClient()
        val coordinator = MediaTransferCoordinator(client, device(), "credential")
        coordinator.enqueue(listOf(source("a")))
        coordinator.uploadPending()

        coordinator.delete("a")

        assertEquals(emptyList(), coordinator.items)
        assertEquals(listOf("a"), client.deleted)
    }

    private class FakeClient(
        private val failOnce: MutableSet<String> = mutableSetOf(),
    ) : MediaTransferClient {
        val uploaded = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        override fun upload(
            device: DiscoveredTvService,
            credentialId: String,
            source: LocalMediaSource,
            onProgress: (Long, Long) -> Unit,
        ): Result<Unit> {
            uploaded += source.mediaId
            onProgress(source.byteSize, source.byteSize)
            return if (failOnce.remove(source.mediaId)) Result.failure(IllegalStateException("temporary")) else Result.success(Unit)
        }

        override fun delete(device: DiscoveredTvService, credentialId: String, mediaId: String): Result<Unit> {
            deleted += mediaId
            return Result.success(Unit)
        }
    }

    private fun source(id: String) = object : LocalMediaSource {
        override val mediaId = id
        override val filename = "$id.jpg"
        override val mimeType = "image/jpeg"
        override val byteSize = 3L
        override val sha256 = "a".repeat(64)
        override fun openStream() = ByteArrayInputStream(byteArrayOf(1, 2, 3))
    }

    private fun device() = DiscoveredTvService("TV", "192.0.2.10", 12345, 1)
}
